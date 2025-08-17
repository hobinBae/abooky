package com.c203.autobiography.domain.communityBook.repository;

import com.c203.autobiography.domain.book.dto.BookType;
import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
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

    /**
     * 제목 키워드와 태그로 커뮤니티 책 검색 (통합 검색)
     */
    @Query("SELECT DISTINCT cb FROM CommunityBook cb " +
            "LEFT JOIN CommunityBookTag cbt ON cb.communityBookId = cbt.communityBook.communityBookId " +
            "LEFT JOIN cbt.tag t " +
            "WHERE cb.deletedAt IS NULL " +
            "AND (:title IS NULL OR LOWER(cb.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:categoryId IS NULL OR cb.category.bookCategoryId = :categoryId) " +
            "AND (:bookType IS NULL OR cb.bookType = :bookType) " +
            "AND (:tags IS NULL OR t.tagName IN :tags)")
    Page<CommunityBook> findBooksWithFiltersAndSearch(
            @Param("title") String title,
            @Param("tags") List<String> tags,
            @Param("categoryId") Long categoryId,
            @Param("bookType") BookType bookType,
            Pageable pageable
    );

    /**
     * 모든 태그가 포함된 책만 검색하는 버전 (AND 조건)
     */
    @Query("SELECT DISTINCT cb FROM CommunityBook cb " +
            "WHERE cb.deletedAt IS NULL " +
            "AND (:title IS NULL OR LOWER(cb.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:categoryId IS NULL OR cb.category.bookCategoryId = :categoryId) " +
            "AND (:bookType IS NULL OR cb.bookType = :bookType) " +
            "AND (:tags IS NULL OR " +
            "     (SELECT COUNT(DISTINCT cbt2.tag.tagName) " +
            "      FROM CommunityBookTag cbt2 " +
            "      WHERE cbt2.communityBook.communityBookId = cb.communityBookId " +
            "      AND cbt2.tag.tagName IN :tags) = :tagCount)")
    Page<CommunityBook> findBooksWithAllTagsRequired(
            @Param("title") String title,
            @Param("tags") List<String> tags,
            @Param("tagCount") long tagCount,
            @Param("categoryId") Long categoryId,
            @Param("bookType") BookType bookType,
            Pageable pageable
    );

    /**
     * 제목으로만 검색
     */
    @Query("SELECT cb FROM CommunityBook cb " +
            "WHERE cb.deletedAt IS NULL " +
            "AND LOWER(cb.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
            "AND (:categoryId IS NULL OR cb.category.bookCategoryId = :categoryId) " +
            "AND (:bookType IS NULL OR cb.bookType = :bookType)")
    Page<CommunityBook> findByTitleContainingIgnoreCase(
            @Param("title") String title,
            @Param("categoryId") Long categoryId,
            @Param("bookType") BookType bookType,
            Pageable pageable
    );

    /**
     * 태그로만 검색
     */
    @Query("SELECT DISTINCT cb FROM CommunityBook cb " +
            "JOIN CommunityBookTag cbt ON cb.communityBookId = cbt.communityBook.communityBookId " +
            "JOIN cbt.tag t " +
            "WHERE cb.deletedAt IS NULL " +
            "AND t.tagName IN :tags " +
            "AND (:categoryId IS NULL OR cb.category.bookCategoryId = :categoryId) " +
            "AND (:bookType IS NULL OR cb.bookType = :bookType)")
    Page<CommunityBook> findByTagsIn(
            @Param("tags") List<String> tags,
            @Param("categoryId") Long categoryId,
            @Param("bookType") BookType bookType,
            Pageable pageable
    );
}
