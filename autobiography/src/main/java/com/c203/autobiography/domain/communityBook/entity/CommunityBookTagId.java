package com.c203.autobiography.domain.communityBook.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class CommunityBookTagId implements Serializable {

    @Column(name = "community_book_id")
    private Long communityBookId;

    @Column(name = "tag_id")
    private Long tagId;

    public static CommunityBookTagId of(Long communityBookId, Long tagId) {
        return CommunityBookTagId.builder()
                .communityBookId(communityBookId)
                .tagId(tagId)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommunityBookTagId that = (CommunityBookTagId) o;
        return Objects.equals(communityBookId, that.communityBookId) &&
                Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(communityBookId, tagId);
    }
}