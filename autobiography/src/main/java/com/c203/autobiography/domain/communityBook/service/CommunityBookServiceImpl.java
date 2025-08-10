package com.c203.autobiography.domain.communityBook.service;

import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import com.c203.autobiography.domain.communityBook.repository.CommunityBookRepository;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommunityBookServiceImpl implements CommunityBookService {

    private final MemberRepository memberRepository;
    private final CommunityBookRepository communityBookRepository;

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
}
