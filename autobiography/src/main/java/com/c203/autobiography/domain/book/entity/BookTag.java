package com.c203.autobiography.domain.book.entity;

import com.c203.autobiography.domain.episode.entity.Episode;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_tags",
        indexes = @Index(name = "idx_book_tags_tag_id", columnList = "tag_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class BookTag {
    @EmbeddedId
    private BookTagId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("bookId")
    @JoinColumn(
            name = "book_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_book_tag_book")
    )
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tagId")
    @JoinColumn(
            name = "tag_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_book_tag_tag")
    )
    private Tag tag;


    /**
     * 관계를 생성합니다.
     */
    public static BookTag of(Book book, Tag tag) {
        return BookTag.builder()
                .id(BookTagId.of(book.getBookId(), tag.getTagId()))
                .book(book)
                .tag(tag)
                .build();
    }
}
