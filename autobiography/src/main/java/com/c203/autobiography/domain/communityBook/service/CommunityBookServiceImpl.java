package com.c203.autobiography.domain.communityBook.service;

import com.c203.autobiography.domain.communityBook.dto.*;
import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookComment;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookEpisode;
import com.c203.autobiography.domain.communityBook.repository.CommunityBookCommentRepository;
import com.c203.autobiography.domain.communityBook.repository.CommunityBookEpisodeRepository;
import com.c203.autobiography.domain.communityBook.repository.CommunityBookRepository;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommunityBookServiceImpl implements CommunityBookService {

    private final MemberRepository memberRepository;
    private final CommunityBookRepository communityBookRepository;
    private final CommunityBookEpisodeRepository communityBookEpisodeRepository;
    private final CommunityBookCommentRepository communityBookCommentRepository;

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

        // 응답 객체 생성
        return CommunityBookDetailResponse.of(communityBook, episodes);
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

}
