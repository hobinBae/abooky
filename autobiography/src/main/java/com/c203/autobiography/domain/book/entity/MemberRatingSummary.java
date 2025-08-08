package com.c203.autobiography.domain.book.entity;

import com.c203.autobiography.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "member_rating_summary")
public class MemberRatingSummary {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;

    @Column(name = "average_rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal averageRating;

    @Column(name = "rating_count", nullable = false, updatable = false)
    private Integer ratingCount;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public void update(BigDecimal avg, int count){
        this.averageRating = avg;
        this.ratingCount = count;
    }

    public static MemberRatingSummary init(Long memberId) {
        return MemberRatingSummary.builder()
                .memberId(memberId)
                .averageRating(BigDecimal.valueOf(0.0))
                .ratingCount(0)
                .build();
    }
}
