package com.c203.autobiography.domain.book.entity;


import com.c203.autobiography.domain.member.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "rating")
public class Rating {

    @EmbeddedId
    private RatingId id;

    @MapsId("bookId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Min(1) @Max(5)
    @Column(name ="score", nullable = false)
    private Integer score;

    // DB에서 기본값/갱신 처리하지만, 널 방지용 필드도 둠
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public void updateScore(Integer newScore) {
        this.score = newScore;
    }

    public static Rating create(Book book, Member member, Integer score) {
        return Rating.builder()
                .id(new RatingId(book.getBookId(), member.getMemberId()))
                .book(book)
                .member(member)
                .score(score)
                .build();
    }
}
