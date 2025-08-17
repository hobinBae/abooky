package com.c203.autobiography.domain.communityBook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class CommunityBookBookmarkId implements Serializable {

    @Column(name = "community_book_id", nullable = false)
    private Long communityBookId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * 복합키 생성 팩토리 메서드
     */
    public static CommunityBookBookmarkId of(Long communityBookId, Long memberId) {
        return new CommunityBookBookmarkId(communityBookId, memberId);
    }
}