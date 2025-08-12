package com.c203.autobiography.domain.communityBook.repository;

import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookLike;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookLikeId;
import com.c203.autobiography.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommunityBookLikeRepository extends JpaRepository<CommunityBookLike, CommunityBookLikeId> {
    boolean existsByCommunityBookAndMember(CommunityBook communityBook, Member member);

    void deleteByCommunityBookAndMember(CommunityBook communityBook, Member member);
}
