package com.c203.autobiography.domain.communityBook.repository;

import com.c203.autobiography.domain.communityBook.entity.*;
import com.c203.autobiography.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommunityBookBookmarkRepository extends JpaRepository<CommunityBookBookmark, CommunityBookBookmarkId>{

    boolean existsByCommunityBookAndMember(CommunityBook communityBook, Member member);

    void deleteByCommunityBookAndMember(CommunityBook communityBook, Member member);
}
