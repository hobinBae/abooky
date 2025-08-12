package com.c203.autobiography.domain.communityBook.repository;

import com.c203.autobiography.domain.book.dto.BookType;
import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CommunityBookRepository extends JpaRepository<CommunityBook, Long>, JpaSpecificationExecutor<CommunityBook> {
    Optional<CommunityBook> findByCommunityBookIdAndDeletedAtIsNull(Long communityBookId);

    @Query(value = "SELECT * FROM community_book cb WHERE cb.community_book_id = :communityBookId", nativeQuery = true)
    Optional<CommunityBook> findByIdIgnoreDeleted(@Param("communityBookId") Long communityBookId);

    @Query("SELECT cb FROM CommunityBook cb WHERE cb.category.bookCategoryId = :categoryId AND cb.bookType = :bookType AND cb.deletedAt IS NULL")
    Page<CommunityBook> findByCategoryIdAndBookTypeAndDeletedAtIsNull(Long categoryId, BookType bookType, Pageable pageable);

    @Query("SELECT cb FROM CommunityBook cb WHERE cb.category.bookCategoryId = :categoryId AND cb.deletedAt IS NULL")
    Page<CommunityBook> findByCategoryIdAndDeletedAtIsNull(Long categoryId, Pageable pageable);

    Page<CommunityBook> findByBookTypeAndDeletedAtIsNull(BookType bookTypeEnum, Pageable pageable);

    Page<CommunityBook> findByDeletedAtIsNull(Pageable pageable);

    @Query("SELECT cb FROM CommunityBook cb WHERE cb.member.memberId = :memberId AND cb.deletedAt IS NULL")
    Page<CommunityBook> findByMemberMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    @Query("SELECT cb FROM CommunityBook cb WHERE cb.member.memberId = :memberId AND cb.category.bookCategoryId = :categoryId AND cb.bookType = :bookType AND cb.deletedAt IS NULL")
    Page<CommunityBook> findByMemberMemberIdAndCategoryIdAndBookTypeAndDeletedAtIsNull(Long memberId, Long categoryId, BookType bookTypeEnum, Pageable pageable);

    @Query("SELECT cb FROM CommunityBook cb WHERE cb.member.memberId = :memberId AND cb.category.bookCategoryId = :categoryId AND cb.deletedAt IS NULL")
    Page<CommunityBook> findByMemberMemberIdAndCategoryIdAndDeletedAtIsNull(Long memberId, Long categoryId, Pageable pageable);

    @Query("SELECT cb FROM CommunityBook cb WHERE cb.member.memberId = :memberId AND cb.bookType = :bookType AND cb.deletedAt IS NULL")
    Page<CommunityBook> findByMemberMemberIdAndBookTypeAndDeletedAtIsNull(Long memberId, BookType bookTypeEnum, Pageable pageable);

}
