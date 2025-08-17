package com.c203.autobiography.domain.communityBook.repository;

import com.c203.autobiography.domain.communityBook.entity.*;
import com.c203.autobiography.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityBookBookmarkRepository extends JpaRepository<CommunityBookBookmark, CommunityBookBookmarkId>{

    boolean existsByCommunityBookAndMember(CommunityBook communityBook, Member member);

    void deleteByCommunityBookAndMember(CommunityBook communityBook, Member member);

    @Query("SELECT cb.communityBook FROM CommunityBookBookmark cb " +
           "WHERE cb.member.memberId = :memberId " +
           "ORDER BY cb.createdAt DESC")
    Page<CommunityBook> findBookmarkedCommunityBooksByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
