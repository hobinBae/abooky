package com.c203.autobiography.domain.communityBook.entity;

import com.c203.autobiography.domain.book.entity.Tag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "community_book_tags",
        indexes = @Index(name = "idx_community_book_tags_tag_id", columnList = "tag_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class CommunityBookTag {

    @EmbeddedId
    private CommunityBookTagId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("communityBookId")
    @JoinColumn(
            name = "community_book_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_community_book_tag_book")
    )
    private CommunityBook communityBook;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tagId")
    @JoinColumn(
            name = "tag_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_community_book_tag_tag")
    )
    private Tag tag;

    /**
     * 관계를 생성합니다.
     */
    public static CommunityBookTag of(CommunityBook communityBook, Tag tag) {
        return CommunityBookTag.builder()
                .id(CommunityBookTagId.of(communityBook.getCommunityBookId(), tag.getTagId()))
                .communityBook(communityBook)
                .tag(tag)
                .build();
    }
}