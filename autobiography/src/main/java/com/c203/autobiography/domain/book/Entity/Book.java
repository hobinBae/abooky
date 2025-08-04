package com.c203.autobiography.domain.book.Entity;

import com.c203.autobiography.domain.book.dto.BookType;
import com.c203.autobiography.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "book")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
// 논리 삭제 적용
@SQLDelete(sql = "UPDATE book SET deleted_at = CURRENT_TIMESTAMP WHERE book_id = ?")
@Where(clause = "deleted_at IS NULL")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String title;

    @Size(max = 255)
    @Column(name = "cover_image_url", length = 100)
    private String coverImageUrl;

    @Lob
    @Column
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_book_member")
    )
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_type", nullable = false, length = 20)
    private BookType bookType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "category_id",
            foreignKey = @ForeignKey(name = "fk_book_category")
    )
    private BookCategory category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(
            name = "average_rating",
            precision = 2,
            scale = 1,
            nullable = false
    )
    private BigDecimal averageRating = BigDecimal.valueOf(0.0);

    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // — 도메인 행위 메서드 —

    /** 책 정보를 업데이트합니다. */
    public void updateDetails(
            String title,
            String coverImageUrl,
            String summary,
            BookCategory category
    ) {
        this.title = title;
        this.coverImageUrl = coverImageUrl;
        this.summary = summary;
        this.category = category;
    }

    /** 좋아요 수를 1 증가시킵니다. */
    public void incrementLike() {
        this.likeCount++;
    }

    /** 조회 수를 1 증가시킵니다. */
    public void incrementView() {
        this.viewCount++;
    }
    /**
     * 평균 평점을 갱신합니다.
     * @param newAverage 소수점 한 자리까지 반영된 새로운 평균
     */
    public void updateAverageRating(BigDecimal newAverage) {
        this.averageRating = newAverage;
    }

    /** 완료 상태로 전환하고 완료 시간을 기록합니다. */
    public void markCompleted() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }

//    /** 완료 상태를 해제합니다. (필요 시) */
//    public void unmarkCompleted() {
//        this.completed = false;
//        this.completedAt = null;
//    }

}
