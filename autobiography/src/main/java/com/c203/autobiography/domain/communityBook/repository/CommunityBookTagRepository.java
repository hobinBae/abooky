package com.c203.autobiography.domain.communityBook.repository;

import com.c203.autobiography.domain.communityBook.dto.CommunityBookTagResponse;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityBookTagRepository extends JpaRepository<CommunityBookTag, Long> {
    @Query("SELECT new com.c203.autobiography.domain.communityBook.dto.CommunityBookTagResponse(" +
            "t.tagId, t.tagName) " +
            "FROM CommunityBookTag cbt " +
            "JOIN cbt.tag t " +
            "WHERE cbt.communityBook.communityBookId = :communityBookId " +
            "ORDER BY t.tagName")
    List<CommunityBookTagResponse> findTagInfoByCommunityBookId(@Param("communityBookId") Long communityBookId);

}
