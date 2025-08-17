package com.c203.autobiography.domain.book.entity;

import com.c203.autobiography.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookLike {
    @EmbeddedId
    private BookLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_like_book"))
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_like_member"))
    private Member member;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 좋아요 이력 생성 팩토리 메서드
     */
    public static BookLike of(Book book, Member member) {
        return BookLike.builder()
                .id(new BookLikeId(book.getBookId(), member.getMemberId()))
                .book(book)
                .member(member)
                .build();
    }

}
