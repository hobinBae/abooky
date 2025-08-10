package com.c203.autobiography.domain.communityBook.repository;

import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CommunityBookRepository extends JpaRepository<CommunityBook, Long>, JpaSpecificationExecutor<CommunityBook> {
    Optional<CommunityBook> findByCommunityBookIdAndDeletedAtIsNull(Long communityBookId);

    @Query(value = "SELECT * FROM community_book cb WHERE cb.community_book_id = :communityBookId", nativeQuery = true)
    Optional<CommunityBook> findByIdIgnoreDeleted(@Param("communityBookId") Long communityBookId);
}
