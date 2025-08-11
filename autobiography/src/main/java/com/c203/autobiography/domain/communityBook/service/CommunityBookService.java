package com.c203.autobiography.domain.communityBook.service;

import com.c203.autobiography.domain.communityBook.dto.CommunityBookCommentRequest;
import com.c203.autobiography.domain.communityBook.dto.CommunityBookCommentResponse;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

public interface CommunityBookService {
    void deleteCommunityBook(Long memberId, Long communityBookId);

    // 댓글 관련 기능
    CommunityBookCommentResponse createCommunityBookComment(Long memberId, @Valid CommunityBookCommentRequest request);
}
