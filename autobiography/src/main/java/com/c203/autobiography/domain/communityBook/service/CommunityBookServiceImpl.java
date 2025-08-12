package com.c203.autobiography.domain.communityBook.service;

import com.c203.autobiography.domain.book.dto.BookType;
import com.c203.autobiography.domain.book.dto.LikeResponse;
import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.book.entity.BookLike;
import com.c203.autobiography.domain.communityBook.dto.*;
import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookComment;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookEpisode;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookLike;
import com.c203.autobiography.domain.communityBook.repository.*;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommunityBookServiceImpl implements CommunityBookService {

    private final MemberRepository memberRepository;
    private final CommunityBookRepository communityBookRepository;
    private final CommunityBookEpisodeRepository communityBookEpisodeRepository;
    private final CommunityBookCommentRepository communityBookCommentRepository;
    private final CommunityBookTagRepository communityBookTagRepository;
    private final CommunityBookLikeRepository communityBookLikeRepository;

    @Transactional(readOnly = true)
    @Override
    public CommunityBookListResponse search(Long memberId, Pageable pageable, String title, String[] tags, Long categoryId, String bookType, String sortBy) {
        // 탈퇴한 회원인 경우
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 1. 정렬 기준 적용
        Pageable sortedPageable = applySorting(pageable, sortBy);

        // 2. 검색 조건 처리
        String titleCondition = processTitle(title);
        List<String> tagConditions = processTags(tags);

        // 3. 필터링 조건에 따른 조회
        Page<CommunityBook> communityBooks = getCommunityBooksWithFilters(
                sortedPageable, titleCondition, tagConditions, categoryId, bookType);

        // 4. 모든 책의 ID 추출
        List<Long> communityBookIds = communityBooks.getContent()
                .stream()
                .map(CommunityBook::getCommunityBookId)
                .collect(Collectors.toList());

        // 5. 태그 정보 일괄 조회 (N+1 문제 방지)
        Map<Long, List<CommunityBookTagResponse>> tagMap = getCommunityBookTagsMap(communityBookIds);

        // 6. DTO 변환 (태그 정보 포함)
        Page<CommunityBookSummaryResponse> responsePage = communityBooks.map(book -> {
            List<CommunityBookTagResponse> bookTags = tagMap.getOrDefault(book.getCommunityBookId(), new ArrayList<>());
            return CommunityBookSummaryResponse.of(book, bookTags);
        });

        return CommunityBookListResponse.of(responsePage);
    }

    /**
     * 제목 검색 조건 처리
     */
    private String processTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        return title.trim();
    }

    /**
     * 태그 검색 조건 처리
     */
    private List<String> processTags(String[] tags) {
        if (tags == null || tags.length == 0) {
            return null;
        }

        return Arrays.stream(tags)
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .map(String::trim)
                .distinct() // 중복 제거
                .collect(Collectors.toList());
    }

    /**
     * 필터링 조건에 따른 커뮤니티 책 조회 (검색 기능 추가)
     */
    private Page<CommunityBook> getCommunityBooksWithFilters(Pageable pageable, String title, List<String> tags, Long categoryId, String bookType) {
        // BookType enum 변환
        BookType bookTypeEnum = null;
        if (bookType != null && !bookType.trim().isEmpty()) {
            try {
                bookTypeEnum = BookType.valueOf(bookType.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND);
            }
        }

        // Repository 메서드 호출 (검색 조건 포함)
        return communityBookRepository.findBooksWithFiltersAndSearch(
                title, tags, categoryId, bookTypeEnum, pageable);
    }

    /**
     * 정렬 기준 적용
     */
    private Pageable applySorting(Pageable pageable, String sortBy) {
        Sort sort = switch (sortBy.toLowerCase()) {
            case "recent" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "liked" -> Sort.by(Sort.Direction.DESC, "likeCount");
            case "rating" -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "title" -> Sort.by(Sort.Direction.ASC, "title");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    /**
     * 여러 커뮤니티 책의 태그 정보를 한 번에 조회하여 Map으로 반환 (N+1 문제 방지)
     */
    private Map<Long, List<CommunityBookTagResponse>> getCommunityBookTagsMap(List<Long> communityBookIds) {
        if (communityBookIds.isEmpty()) {
            return new HashMap<>();
        }

        // 모든 태그 정보를 한 번에 조회
        Map<Long, List<CommunityBookTagResponse>> tagMap = new HashMap<>();

        for (Long bookId : communityBookIds) {
            List<CommunityBookTagResponse> tags = communityBookTagRepository.findTagInfoByCommunityBookId(bookId);
            tagMap.put(bookId, tags);
        }

        return tagMap;
    }


    @Transactional
    @Override
    public CommunityBookDetailResponse getCommunityBookDetail(Long memberId, Long communityBookId) {
        // 1. 탈퇴한 회원인 경우
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 아예 존재하지 않는 책인 경우 (테이블 자체에 데이터가 없는 경우)
        if (communityBookRepository.findByIdIgnoreDeleted(communityBookId).isEmpty()) {
            throw new ApiException(ErrorCode.COMMUNITY_BOOK_NOT_FOUND);
        }

        // 3. 커뮤니티 책이 존재하지 않는 경우 & 책 조회
        CommunityBook communityBook = communityBookRepository.findByCommunityBookIdAndDeletedAtIsNull(communityBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_BOOK_ALREADY_DELETED));

        // 에피소드 조회 (삭제되지 않은 것만, 순서대로)
        List<CommunityBookEpisode> episodes = communityBookEpisodeRepository
                .findByCommunityBookCommunityBookIdAndDeletedAtIsNullOrderByEpisodeOrderAsc(communityBookId);

        // 조회수 증가
        incrementViewCount(communityBook);

        // 태그 조회
        List<CommunityBookTagResponse> tags = communityBookTagRepository.findTagInfoByCommunityBookId(communityBookId);

        // 응답 객체 생성
        return CommunityBookDetailResponse.of(communityBook, episodes, tags);
    }

    /**
     * 조회수 증가 (더티 체킹으로 자동 저장)
     */
    private void incrementViewCount(CommunityBook communityBook) {
        try {
            communityBook.incrementViewCount();
            log.debug("Incremented view count for community book: {}", communityBook.getCommunityBookId());
        } catch (Exception e) {
            log.error("Failed to increment view count for community book: {}",
                    communityBook.getCommunityBookId(), e);
            // 조회수 증가 실패는 전체 조회를 실패시키지 않음
        }
    }


    @Transactional
    @Override
    public CommunityBookListResponse getCommunityBookList(Long memberId, Pageable pageable, String sortBy) {
        // 탈퇴한 회원인 경우
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 1. 정렬 기준 적용
        Pageable sortedPageable = applySorting(pageable, sortBy);

        // 2. 필터링 조건에 따른 조회
        Page<CommunityBook> communityBooks = getCommunityBooksWithFilters(sortedPageable, null, null, null, null);

        // 3. 모든 책의 ID 추출
        List<Long> communityBookIds = communityBooks.getContent()
                .stream()
                .map(CommunityBook::getCommunityBookId)
                .collect(Collectors.toList());

        // 4. 태그 정보 일괄 조회 (N+1 문제 방지)
        Map<Long, List<CommunityBookTagResponse>> tagMap = getCommunityBookTagsMap(communityBookIds);

        // 5. DTO 변환 (태그 정보 포함)
        Page<CommunityBookSummaryResponse> responsePage = communityBooks.map(book -> {
            List<CommunityBookTagResponse> tags = tagMap.getOrDefault(book.getCommunityBookId(), new ArrayList<>());
            return CommunityBookSummaryResponse.of(book, tags);
        });

        return CommunityBookListResponse.of(responsePage);
    }


    @Transactional
    @Override
    public CommunityBookListResponse getMemberCommunityBooks(Long userId, Long memberId, Pageable pageable, Long categoryId, String bookType, String sortBy) {
        // 1. 탈퇴한 회원인 경우
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 작가 존재 여부 확인
        validateMemberExists(memberId);

        // 정렬 기준 적용
        Pageable sortedPageable = applySorting(pageable, sortBy);

        // 필터링 조건에 따른 조회
        Page<CommunityBook> communityBooks = getMemberCommunityBooksWithFilters(
                memberId, sortedPageable, categoryId, bookType);

        // 3. 모든 책의 ID 추출
        List<Long> communityBookIds = communityBooks.getContent()
                .stream()
                .map(CommunityBook::getCommunityBookId)
                .collect(Collectors.toList());

        // 4. 태그 정보 일괄 조회 (N+1 문제 방지)
        Map<Long, List<CommunityBookTagResponse>> tagMap = getCommunityBookTagsMap(communityBookIds);

        // 5. DTO 변환 (태그 정보 포함)
        Page<CommunityBookSummaryResponse> responsePage = communityBooks.map(book -> {
            List<CommunityBookTagResponse> tags = tagMap.getOrDefault(book.getCommunityBookId(), new ArrayList<>());
            return CommunityBookSummaryResponse.of(book, tags);
        });

        return CommunityBookListResponse.of(responsePage);
    }

    /**
     * 작가 존재 여부 검증
     */
    private void validateMemberExists(Long memberId) {
        boolean exists = memberRepository.existsByMemberIdAndDeletedAtIsNull(memberId);
        if (!exists) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * 작가별 필터링 조건에 따른 커뮤니티 책 조회
     */
    private Page<CommunityBook> getMemberCommunityBooksWithFilters(Long memberId, Pageable pageable, Long categoryId, String bookType) {
        // BookType enum 변환
        BookType bookTypeEnum = null;
        if (bookType != null && !bookType.trim().isEmpty()) {
            try {
                bookTypeEnum = BookType.valueOf(bookType.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND);
            }
        }

        // 필터링 조건에 따른 조회
        if (categoryId != null && bookTypeEnum != null) {
            return communityBookRepository.findByMemberMemberIdAndCategoryIdAndBookTypeAndDeletedAtIsNull(
                    memberId, categoryId, bookTypeEnum, pageable);
        } else if (categoryId != null) {
            return communityBookRepository.findByMemberMemberIdAndCategoryIdAndDeletedAtIsNull(
                    memberId, categoryId, pageable);
        } else if (bookTypeEnum != null) {
            return communityBookRepository.findByMemberMemberIdAndBookTypeAndDeletedAtIsNull(
                    memberId, bookTypeEnum, pageable);
        } else {
            return communityBookRepository.findByMemberMemberIdAndDeletedAtIsNull(memberId, pageable);
        }
    }

    /**
     * CommunityBook을 CommunityBookSummaryResponse로 변환 (태그 정보 포함)
     */
    private CommunityBookSummaryResponse convertToSummaryResponse(CommunityBook communityBook) {
        // 해당 책의 태그 정보 조회
        List<CommunityBookTagResponse> tags = communityBookTagRepository.findTagInfoByCommunityBookId(communityBook.getCommunityBookId());

        return CommunityBookSummaryResponse.of(communityBook, tags);
    }


    @Transactional
    @Override
    public void deleteCommunityBook(Long memberId, Long communityBookId) {
        // 1. 탈퇴한 회원인 경우
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 아예 존재하지 않는 책인 경우 (테이블 자체에 데이터가 없는 경우)
        if (communityBookRepository.findByIdIgnoreDeleted(communityBookId).isEmpty()) {
            throw new ApiException(ErrorCode.COMMUNITY_BOOK_NOT_FOUND);
        }

        // 3. 커뮤니티 책이 존재하지 않는 경우
        CommunityBook communityBook = communityBookRepository.findByCommunityBookIdAndDeletedAtIsNull(communityBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_BOOK_ALREADY_DELETED));

        // 4 현재 로그인한 회원이 책의 저자가 아닌 경우
        if(!communityBook.getMember().getMemberId().equals(memberId)){
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        communityBook.softDelete();
    }

    @Transactional
    @Override
    public CommunityBookCommentCreateResponse createCommunityBookComment(Long memberId, CommunityBookCommentCreateRequest request) {
        // 1. 탈퇴한 회원인 경우
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 아예 존재하지 않는 책인 경우 (테이블 자체에 데이터가 없는 경우)
        if (communityBookRepository.findByIdIgnoreDeleted(request.getCommunityBookId()).isEmpty()) {
            throw new ApiException(ErrorCode.COMMUNITY_BOOK_NOT_FOUND);
        }

        // 3. 커뮤니티 책이 존재하지 않는 경우
        CommunityBook communityBook = communityBookRepository.findByCommunityBookIdAndDeletedAtIsNull(request.getCommunityBookId())
                .orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_BOOK_ALREADY_DELETED));

        // 댓글 엔티티 생성
        CommunityBookComment comment = CommunityBookComment.builder()
                .content(request.getContent())
                .communityBook(communityBook)
                .member(member)
                .build();

        // 댓글 저장
        CommunityBookComment savedComment = communityBookCommentRepository.save(comment);
        return CommunityBookCommentCreateResponse.of(savedComment);
    }

    @Override
    public CommunityBookCommentListResponse getCommunityCookComments(Long memberId, Long communityBookId, Pageable pageable) {
        // 1. 탈퇴한 회원인 경우
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 아예 존재하지 않는 책인 경우 (테이블 자체에 데이터가 없는 경우)
        if (communityBookRepository.findByIdIgnoreDeleted(communityBookId).isEmpty()) {
            throw new ApiException(ErrorCode.COMMUNITY_BOOK_NOT_FOUND);
        }

        // 3. 커뮤니티 책이 존재하지 않는 경우
        CommunityBook communityBook = communityBookRepository.findByCommunityBookIdAndDeletedAtIsNull(communityBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_BOOK_ALREADY_DELETED));

        // 댓글 조회하기
        Page<CommunityBookComment> comments = communityBookCommentRepository
                .findByCommunityBookCommunityBookIdOrderByCreatedAtAsc(communityBookId, pageable);

        System.out.println("조회된 댓글 수: "+ comments.getTotalElements());
        System.out.println("댓글 내용: "+comments.getContent());

        Page<CommunityBookCommentDetailResponse> commentResponses = comments.map(CommunityBookCommentDetailResponse::of);

        return CommunityBookCommentListResponse.of(commentResponses);
    }

    @Transactional
    @Override
    public CommunityBookCommentDeleteResponse deleteCommunityBookComment(Long communityBookId, Long communityBookCommentId, Long memberId) {
        // 1. 탈퇴한 회원인 경우
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 아예 존재하지 않는 책인 경우 (테이블 자체에 데이터가 없는 경우)
        if (communityBookRepository.findByIdIgnoreDeleted(communityBookId).isEmpty()) {
            throw new ApiException(ErrorCode.COMMUNITY_BOOK_NOT_FOUND);
        }

        // 3. 커뮤니티 책이 존재하지 않는 경우
        CommunityBook communityBook = communityBookRepository.findByCommunityBookIdAndDeletedAtIsNull(communityBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_BOOK_ALREADY_DELETED));

        //  댓글 조회하기
        CommunityBookComment comment = communityBookCommentRepository
                .findByCommunityBookCommentIdAndCommunityBookCommunityBookId(communityBookCommentId, communityBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));

        // 4. 댓글 작성자가 아닌 경우
        if (!comment.getMember().getMemberId().equals(member.getMemberId())) {
            throw new ApiException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        // 댓글 삭제하기 (소프트 삭제)
        comment.softDelete();

        return CommunityBookCommentDeleteResponse.of(communityBookCommentId);
    }

    @Override
    @Transactional
    public boolean toggleLike(Long communityBookId, Long memberId) {
        // 1. 멤버 존재 여부 확인
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 커뮤니티 북 존재 여부 확인
        CommunityBook communityBook = communityBookRepository.findByCommunityBookIdAndDeletedAtIsNull(communityBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        // 3. 현재 좋아요 상태 확인
        boolean exists = communityBookLikeRepository.existsByCommunityBookAndMember(communityBook, member);

        if (!exists) {
            // 좋아요 추가
            CommunityBookLike like = CommunityBookLike.of(communityBook, member);
            communityBookLikeRepository.save(like);
            communityBook.incrementLike();

            log.info("User {} liked community book {}", memberId, communityBookId);
            return true; // 좋아요 추가됨
        } else {
            // 좋아요 취소
            communityBookLikeRepository.deleteByCommunityBookAndMember(communityBook, member);
            communityBook.decrementLike();

            log.info("User {} unliked community book {}", memberId, communityBookId);
            return false; // 좋아요 취소됨
        }
    }

    /**
     * 커뮤니티 북 좋아요 수 조회
     */
    @Transactional(readOnly = true)
    public long getLikeCount(Long communityBookId) {
        CommunityBook communityBook = communityBookRepository.findByCommunityBookIdAndDeletedAtIsNull(communityBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        return communityBookLikeRepository.countByCommunityBook(communityBook);
    }
}
