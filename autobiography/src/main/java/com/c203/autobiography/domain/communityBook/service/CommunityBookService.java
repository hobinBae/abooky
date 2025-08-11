package com.c203.autobiography.domain.communityBook.service;

import com.c203.autobiography.domain.communityBook.dto.CommunityBookCommentCreateRequest;
import com.c203.autobiography.domain.communityBook.dto.CommunityBookCommentCreateResponse;
import jakarta.validation.Valid;

public interface CommunityBookService {
    void deleteCommunityBook(Long memberId, Long communityBookId);

    // 댓글 관련 기능
    CommunityBookCommentCreateResponse createCommunityBookComment(Long memberId, @Valid CommunityBookCommentCreateRequest request);
}
