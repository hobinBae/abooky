package com.c203.autobiography.domain.communityBook.service;

import org.springframework.transaction.annotation.Transactional;

public interface CommunityBookService {
    void deleteCommunityBook(Long memberId, Long communityBookId);
}
