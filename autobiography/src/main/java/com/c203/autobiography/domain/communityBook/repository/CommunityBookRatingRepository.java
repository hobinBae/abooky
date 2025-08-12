package com.c203.autobiography.domain.communityBook.repository;

import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookRating;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookRatingId;
import com.c203.autobiography.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface CommunityBookRatingRepository extends JpaRepository<CommunityBookRating, CommunityBookRatingId> {
    /**
     * 특정 커뮤니티 북과 멤버의 평점 조회
     */
    @Query("SELECT cbr FROM CommunityBookRating cbr " +
            "WHERE cbr.communityBook = :communityBook AND cbr.member = :member")
    Optional<CommunityBookRating> findByCommunityBookAndMember(
            @Param("communityBook") CommunityBook communityBook,
            @Param("member") Member member);

    /**
     * 특정 사용자가 특정 커뮤니티 책에 평점을 매겼는지 확인
     */
    @Query("SELECT cbr FROM CommunityBookRating cbr " +
            "WHERE cbr.communityBook.communityBookId = :communityBookId " +
            "AND cbr.member.memberId = :memberId")
    Optional<CommunityBookRating> findByBookAndMember(
            @Param("communityBookId") Long communityBookId,
            @Param("memberId") Long memberId
    );

    /**
     * 특정 커뮤니티 책의 평균 평점 조회
     */
    @Query("SELECT AVG(CAST(cbr.score AS DOUBLE)) FROM CommunityBookRating cbr " +
            "WHERE cbr.communityBook.communityBookId = :communityBookId")
    Optional<Double> findAverageRatingByCommunityBookId(@Param("communityBookId") Long communityBookId);

    /**
     * 특정 커뮤니티 책의 평점 개수 조회
     */
    @Query("SELECT COUNT(cbr) FROM CommunityBookRating cbr " +
            "WHERE cbr.communityBook.communityBookId = :communityBookId")
    long countByCommunityBookId(@Param("communityBookId") Long communityBookId);

    /**
     * 특정 커뮤니티 책의 평점별 개수 조회
     */
    @Query("SELECT cbr.score, COUNT(cbr) FROM CommunityBookRating cbr " +
            "WHERE cbr.communityBook.communityBookId = :communityBookId " +
            "GROUP BY cbr.score " +
            "ORDER BY cbr.score DESC")
    List<Object[]> findRatingDistributionByCommunityBookId(@Param("communityBookId") Long communityBookId);

    /**
     * 특정 사용자가 평점을 매긴 커뮤니티 책 목록 조회
     */
    @Query("SELECT cbr.communityBook FROM CommunityBookRating cbr " +
            "WHERE cbr.member.memberId = :memberId " +
            "AND cbr.communityBook.deletedAt IS NULL " +
            "ORDER BY cbr.updatedAt DESC")
    Page<CommunityBook> findRatedBooksByMember(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    /**
     * 특정 커뮤니티 책에 평점을 매긴 사용자 목록 조회
     */
    @Query("SELECT cbr FROM CommunityBookRating cbr " +
            "JOIN FETCH cbr.member " +
            "WHERE cbr.communityBook.communityBookId = :communityBookId " +
            "ORDER BY cbr.updatedAt DESC")
    Page<CommunityBookRating> findRatingsByBook(
            @Param("communityBookId") Long communityBookId,
            Pageable pageable
    );

    /**
     * 여러 커뮤니티 책에 대한 사용자의 평점 조회
     */
    @Query("SELECT cbr.communityBook.communityBookId, cbr.score FROM CommunityBookRating cbr " +
            "WHERE cbr.member.memberId = :memberId " +
            "AND cbr.communityBook.communityBookId IN :communityBookIds")
    List<Object[]> findRatingsByMemberAndBooks(
            @Param("memberId") Long memberId,
            @Param("communityBookIds") List<Long> communityBookIds
    );

    /**
     * 특정 사용자의 총 평점 개수
     */
    @Query("SELECT COUNT(cbr) FROM CommunityBookRating cbr " +
            "WHERE cbr.member.memberId = :memberId")
    long countByMemberMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 평점 이상인 커뮤니티 책 조회
     */
    @Query("SELECT cbr.communityBook FROM CommunityBookRating cbr " +
            "WHERE cbr.score >= :minScore " +
            "AND cbr.communityBook.deletedAt IS NULL " +
            "GROUP BY cbr.communityBook " +
            "HAVING AVG(CAST(cbr.score AS DOUBLE)) >= :minScore")
    Page<CommunityBook> findBooksWithMinimumRating(
            @Param("minScore") Integer minScore,
            Pageable pageable
    );
}
