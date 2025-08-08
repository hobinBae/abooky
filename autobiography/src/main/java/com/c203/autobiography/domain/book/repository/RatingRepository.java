package com.c203.autobiography.domain.book.repository;

import com.c203.autobiography.domain.book.entity.Rating;
import com.c203.autobiography.domain.book.entity.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RatingRepository extends JpaRepository<Rating, RatingId> {

    @Query("select avg(r.score) from Rating r where r.book.bookId = :bookId")
    Double findAverageScoreByBookId(Long bookId);

    long countByBook_BookId(Long bookId);

    @Query("select avg(r.score) from Rating r where r.member.memberId = :memberId")
    Double findAverageScoreByMemberId(Long memberId);

    long countByMember_MemberId(Long memberId);
}
