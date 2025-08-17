package com.c203.autobiography.domain.groupbook.entity;

import com.c203.autobiography.domain.book.entity.Tag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_book_tags",
indexes = @Index(name = "idx_group_book_tags_tag_id", columnList = "tag_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class GroupBookTag {
    @EmbeddedId
    private GroupBookTagId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("groupBookId")
    @JoinColumn(
            name = "group_book_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_group_book_tag_group_book")
    )
    private GroupBook groupBook;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tagId")
    @JoinColumn(
            name = "tag_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_group_book_tag_tag")
    )
    private Tag tag;

    public static GroupBookTag of(GroupBook groupBook, Tag tag) {
        return GroupBookTag.builder()
                .id(GroupBookTagId.of(groupBook.getGroupBookId(), tag.getTagId()))
                .groupBook(groupBook).tag(tag).build();
    }
}
