package com.c203.autobiography.domain.communityBook.entity;

import com.c203.autobiography.domain.member.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_book_rating")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CommunityBookRating {

    @EmbeddedId
    private CommunityBookRatingId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("communityBookId")
    @JoinColumn(name = "community_book_id", nullable = false)
    private CommunityBook communityBook;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    @Column(name = "score", nullable = false)
    private Integer score;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    /**
     * 커뮤니티 책 평점 생성 팩토리 메서드
     */
    public static CommunityBookRating of(CommunityBook communityBook, Member member, Integer score) {
        return CommunityBookRating.builder()
                .id(CommunityBookRatingId.of(communityBook.getCommunityBookId(), member.getMemberId()))
                .communityBook(communityBook)
                .member(member)
                .score(score)
                .build();
    }
}