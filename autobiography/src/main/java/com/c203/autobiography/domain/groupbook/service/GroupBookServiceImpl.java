package com.c203.autobiography.domain.groupbook.service;

import com.c203.autobiography.domain.book.dto.BookType;
import com.c203.autobiography.domain.book.entity.BookCategory;
import com.c203.autobiography.domain.book.entity.Tag;
import com.c203.autobiography.domain.book.repository.BookCategoryRepository;
import com.c203.autobiography.domain.book.repository.TagRepository;
import com.c203.autobiography.domain.group.entity.Group;
import com.c203.autobiography.domain.group.repository.GroupMemberRepository;
import com.c203.autobiography.domain.group.repository.GroupRepository;
import com.c203.autobiography.domain.groupbook.dto.*;
import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import com.c203.autobiography.domain.groupbook.entity.GroupBookComment;
import com.c203.autobiography.domain.groupbook.episode.dto.GroupEpisodeResponse;
import com.c203.autobiography.domain.groupbook.episode.repository.GroupEpisodeRepository;
import com.c203.autobiography.domain.groupbook.episode.service.GuideQuestion;
import com.c203.autobiography.domain.groupbook.episode.service.GuideResolverService;
import com.c203.autobiography.domain.groupbook.repository.GroupBookCommentRepository;
import com.c203.autobiography.domain.groupbook.repository.GroupBookRepository;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import com.c203.autobiography.global.s3.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupBookServiceImpl implements GroupBookService {

    private final GroupBookRepository groupBookRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final FileStorageService fileStorageService;
    private final GroupEpisodeRepository episodeRepository;
    private final TagRepository tagRepository;
    private final GuideResolverService guideResolver;
    private final GroupBookCommentRepository groupBookCommentRepository;

    @Override
    @Transactional
    public GroupBookResponse createBook(Long groupId, Long memberId, GroupBookCreateRequest request, MultipartFile file) {
        Group group = groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // BookType이 null인 경우 FREE_FORM으로 기본값 설정
        BookType bookType = (request.getBookType() != null) ? request.getBookType() : BookType.FREE_FORM;
        
        BookCategory category = switch (bookType) {
            case AUTO -> bookCategoryRepository
                    .findByCategoryName("자서전")
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
            case DIARY -> bookCategoryRepository
                    .findByCategoryName("일기")
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
            case FREE_FORM -> {
                if (request.getCategoryId() == null) {
                    // bookType이 명시되지 않고 FREE_FORM으로 기본값 설정된 경우, 기본 카테고리 사용
                    if (request.getBookType() == null) {
                        yield bookCategoryRepository
                                .findByCategoryName("자서전")
                                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
                    } else {
                        // 명시적으로 FREE_FORM을 선택했는데 categoryId가 없는 경우 에러
                        throw new ApiException(ErrorCode.VALIDATION_FAILED);
                    }
                } else {
                    yield bookCategoryRepository
                            .findById(request.getCategoryId())
                            .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
                }
            }
            default -> throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        };

        String coverImageUrl = null;
        if (file != null && !file.isEmpty()) {
            coverImageUrl = fileStorageService.store(file, "groupBook");
        }
        GroupBook book = request.toEntity(member, group, category, coverImageUrl);

        GroupBook saved = groupBookRepository.save(book);

        // 단순히 책 정보만 반환 (에피소드는 별도 생성)
        return GroupBookResponse.of(saved, List.of(), List.of());
    }

    @Override
    @Transactional
    public GroupBookCreateResponse createBookSimple(Long groupId, Long memberId, GroupBookCreateRequest request, MultipartFile file) {
        Group group = groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // BookType이 null인 경우 FREE_FORM으로 기본값 설정
        BookType bookType = (request.getBookType() != null) ? request.getBookType() : BookType.FREE_FORM;
        
        BookCategory category = switch (bookType) {
            case AUTO -> bookCategoryRepository
                    .findByCategoryName("자서전")
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
            case DIARY -> bookCategoryRepository
                    .findByCategoryName("일기")
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
            case FREE_FORM -> {
                if (request.getCategoryId() == null) {
                    // bookType이 명시되지 않고 FREE_FORM으로 기본값 설정된 경우, 기본 카테고리 사용
                    if (request.getBookType() == null) {
                        yield bookCategoryRepository
                                .findByCategoryName("자서전")
                                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
                    } else {
                        // 명시적으로 FREE_FORM을 선택했는데 categoryId가 없는 경우 에러
                        throw new ApiException(ErrorCode.VALIDATION_FAILED);
                    }
                } else {
                    yield bookCategoryRepository
                            .findById(request.getCategoryId())
                            .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
                }
            }
            default -> throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        };

        String coverImageUrl = null;
        if (file != null && !file.isEmpty()) {
            coverImageUrl = fileStorageService.store(file, "groupBook");
        }
        GroupBook book = request.toEntity(member, group, category, coverImageUrl);

        GroupBook saved = groupBookRepository.save(book);

        // 간단한 생성 응답 반환
        return GroupBookCreateResponse.from(saved, null, null);
    }

    @Override
    @Transactional
    public GroupBookResponse updateBook(Long groupId, Long memberId, Long bookId, GroupBookUpdateRequest request, MultipartFile file) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        if(!groupBook.getMember().getMemberId().equals(memberId)){
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        BookCategory category = null;

        if(request.getCategoryId() != null) {
            category = bookCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
        }

        String newImageUrl = groupBook.getCoverImageUrl();
        if(file!=null && !file.isEmpty()) {
            String currentImageUrl = groupBook.getCoverImageUrl();
            if(currentImageUrl != null && !currentImageUrl.isBlank()) {
                fileStorageService.delete(currentImageUrl);
            }
            newImageUrl = fileStorageService.store(file, "groupBook");
        }
        groupBook.updateGroupBook(request.getTitle(), newImageUrl, request.getSummary(), category);
        return GroupBookResponse.of(groupBook);
    }

    @Override
    @Transactional
    public Void deleteBook(Long groupId, Long memberId, Long bookId) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        if(!groupBook.getMember().getMemberId().equals(memberId)){
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        groupBook.softDelete();
        return null;
    }

    @Override
    @Transactional
    public GroupBookResponse completeBook(Long groupId, Long memberId, Long bookId, List<String> tags) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        if(!groupBook.getMember().getMemberId().equals(memberId)){
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        groupBook.markCompleted();

        if(tags!=null && !tags.isEmpty()) {
            groupBook.getTags().clear();
            tags.stream().distinct().forEach(name -> {
                Tag tag = tagRepository.findByTagName(name)
                        .orElseGet(() -> tagRepository.save(
                                Tag.builder().tagName(name).build()
                        ));
                groupBook.addTag(tag);
            });
        }

        List<GroupEpisodeResponse> episodes = episodeRepository
                .findByGroupBook_GroupBookIdAndDeletedAtIsNullOrderByOrderNoAscCreatedAtAsc(bookId)
                .stream()
                .map(GroupEpisodeResponse::of)
                .toList();


        List<String> tagNames = groupBook.getTags().stream()
                .map(bt -> bt.getTag().getTagName())
                .toList();

        return GroupBookResponse.of(groupBook, episodes, tagNames);
    }

    @Override
    public GroupBookResponse getBookDetail(Long groupId, Long memberId, Long bookId) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        boolean isMember = groupMemberRepository.findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, memberId)
                .isPresent();
        if (!isMember) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        // 에피소드 목록 조회 + DTO 매핑
        List<GroupEpisodeResponse> episodes = episodeRepository
                .findByGroupBook_GroupBookIdAndDeletedAtIsNullOrderByOrderNoAscCreatedAtAsc(bookId)
                .stream()
                .map(GroupEpisodeResponse::of)
                .toList();

        // tag
        List<String> tagNames = groupBook.getTags().stream()
                .map(bt -> bt.getTag().getTagName())
                .toList();

        return GroupBookResponse.of(groupBook, episodes, tagNames);
    }

    @Override
    public List<GroupBookResponse> getMyGroupBooks(Long groupId, Long memberId) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        boolean isMember = groupMemberRepository.findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, memberId)
                .isPresent();
        if (!isMember) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        return groupBookRepository
                .findAllByGroupGroupIdAndDeletedAtIsNull(groupId)
                .stream()
                .map(groupBook -> {
                        // 에피소드 목록 조회
                        List<GroupEpisodeResponse> episodes = episodeRepository
                                .findByGroupBook_GroupBookIdAndDeletedAtIsNullOrderByOrderNoAscCreatedAtAsc(groupBook.getGroupBookId())
                                .stream()
                                .map(GroupEpisodeResponse::of)
                                .toList();

                        // 실제 붙어있는 태그 이름만 추출
                        List<String> tagNames = groupBook.getTags().stream()
                                .map(bt-> bt.getTag().getTagName())
                                .toList();

                        return GroupBookResponse.of(groupBook, episodes, tagNames);
                })
                .toList();
    }

    @Override
    public Page<GroupBookResponse> searchBooks(String title, Long categoryId, List<String> tags, Pageable pageable) {
        return null;
    }

    @Transactional
    @Override
    public GroupBookCommentCreateResponse createGroupBookComment(Long memberId, GroupBookCommentCreateRequest request) {
        // 1. 탈퇴한 회원인 경우
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 아예 존재하지 않는 책인 경우 (테이블 자체에 데이터가 없는 경우)
        if (groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(request.getGroupBookId()).isEmpty()) {
            throw new ApiException(ErrorCode.GROUP_BOOK_NOT_FOUND);
        }

        // 3. 커뮤니티 책이 존재하지 않는 경우
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(request.getGroupBookId())
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_BOOK_ALREADY_DELETED));

        // 댓글 엔티티 생성
        GroupBookComment comment = GroupBookComment.builder()
                .content(request.getContent())
                .groupBook(groupBook)
                .member(member)
                .build();

        // 댓글 저장
        GroupBookComment savedComment = groupBookCommentRepository.save(comment);
        return GroupBookCommentCreateResponse.of(savedComment);
    }

    @Override
    @Transactional
    public GroupBookCommentListResponse getGroupBookComments(Long memberId, Long groupBookId, Pageable pageable) {
        // 1. 탈퇴한 회원인 경우
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 아예 존재하지 않는 책인 경우 (테이블 자체에 데이터가 없는 경우)
        if (groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId).isEmpty()) {
            throw new ApiException(ErrorCode.GROUP_BOOK_NOT_FOUND);
        }

        // 3. 커뮤니티 책이 존재하지 않는 경우
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_BOOK_ALREADY_DELETED));

        // 댓글 조회하기
        Page<GroupBookComment> comments = groupBookCommentRepository
                .findByGroupBookGroupBookIdOrderByCreatedAtAsc(groupBookId, pageable);

        System.out.println("조회된 댓글 수: "+ comments.getTotalElements());
        System.out.println("댓글 내용: "+comments.getContent());

        Page<GroupBookCommentDetailResponse> commentResponses = comments.map(GroupBookCommentDetailResponse::of);

        return GroupBookCommentListResponse.of(commentResponses);
    }

    @Transactional
    @Override
    public GroupBookCommentDeleteResponse deleteGroupBookComment(Long groupBookId, Long groupBookCommentId, Long memberId) {
        // 1. 탈퇴한 회원인 경우
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 아예 존재하지 않는 책인 경우 (테이블 자체에 데이터가 없는 경우)
        if (groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId).isEmpty()) {
            throw new ApiException(ErrorCode.GROUP_BOOK_NOT_FOUND);
        }

        // 3. 커뮤니티 책이 존재하지 않는 경우
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_BOOK_ALREADY_DELETED));

        //  댓글 조회하기
        GroupBookComment comment = groupBookCommentRepository
                .findByGroupBookCommentIdAndGroupBookGroupBookId(groupBookCommentId, groupBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));

        // 4. 댓글 작성자가 아닌 경우
        if (!comment.getMember().getMemberId().equals(member.getMemberId())) {
            throw new ApiException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        // 댓글 삭제하기 (소프트 삭제)
        comment.softDelete();

        return GroupBookCommentDeleteResponse.of(groupBookCommentId);
    }
}
