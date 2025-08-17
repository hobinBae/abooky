package com.c203.autobiography.domain.communityBook.repository;

import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookLike;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookLikeId;
import com.c203.autobiography.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface CommunityBookLikeRepository extends JpaRepository<CommunityBookLike, CommunityBookLikeId> {
    boolean existsByCommunityBookAndMember(CommunityBook communityBook, Member member);

    void deleteByCommunityBookAndMember(CommunityBook communityBook, Member member);

    long countByCommunityBook(CommunityBook communityBook);

    @Query("SELECT cbl.communityBook FROM CommunityBookLike cbl WHERE cbl.member.memberId = :memberId ORDER BY cbl.createdAt DESC")
    Page<CommunityBook> findLikedCommunityBooksByMemberId(Long memberId, Pageable pageable);
}
