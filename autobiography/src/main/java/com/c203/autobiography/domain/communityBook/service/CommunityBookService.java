package com.c203.autobiography.domain.communityBook.service;

import com.c203.autobiography.domain.book.dto.LikeResponse;
import com.c203.autobiography.domain.communityBook.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CommunityBookService {
    CommunityBookListResponse search(Long memberId, Pageable pageable, String title, String[] tags, Long categoryId, String bookType, String sortBy);

    void deleteCommunityBook(Long memberId, Long communityBookId);

    // 댓글 관련 기능
    CommunityBookCommentCreateResponse createCommunityBookComment(Long memberId, @Valid CommunityBookCommentCreateRequest request);

    CommunityBookCommentListResponse getCommunityCookComments(Long memberId, Long communityBookId, Pageable pageable);

    CommunityBookCommentDeleteResponse deleteCommunityBookComment(Long communityBookId, Long communityBookCommentId, Long memberId);

    CommunityBookDetailResponse getCommunityBookDetail(Long memberId, Long communityBookId);

    CommunityBookListResponse getCommunityBookList(Long memberId, Pageable pageable, String sortBy);

    CommunityBookListResponse getMemberCommunityBooks(Long userId, Long memberId, Pageable pageable, Long categoryId, String bookType, String sortBy);

    boolean toggleLike(Long bookId, Long memberId);

    long getLikeCount(Long communityBookId);

    boolean toggleBookmark(Long communityBookId, Long memberId);

    CommunityBookRatingResponse createRating(Long memberId, @Valid CommunityBookRatingRequest request);

    CommunityBookRatingResponse getAverageRating(Long communityBookId);
}
