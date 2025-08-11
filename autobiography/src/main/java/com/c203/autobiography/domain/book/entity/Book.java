package com.c203.autobiography.domain.book.entity;

import com.c203.autobiography.domain.book.dto.BookType;
import com.c203.autobiography.domain.member.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(name = "cover_image_url", length = 255)
    private String coverImageUrl;

    @Lob
    @Column(name = "summary", columnDefinition = "TEXT")
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

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Builder.Default
    @Column(
            name = "average_rating",
            precision = 2,
            scale = 1,
            nullable = false
    )
    private BigDecimal averageRating = BigDecimal.valueOf(0.0);

    @Builder.Default
    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 태그 추가
     *
     */
    @OneToMany(
            mappedBy = "book",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BookTag> tags = new ArrayList<>();


    // — 도메인 행위 메서드 —

    /** 책 정보를 업데이트합니다. */
    public void updateBook(
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

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }


    /** 좋아요 수를 1 증가시킵니다. */
    public void incrementLike() {
        this.likeCount++;
    }

    public void decrementLike(){ this.likeCount--; }

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

    /**
     * 태그 전체 초기화
     */
    public void clearTags() {
        tags.clear();
    }

    /**
     * 태그를 추가합니다.
     */
    public void addTag(Tag tag) {
        boolean exists = tags.stream().anyMatch(bt -> bt.getTag().equals(tag));
        if (exists) return;
        tags.add(BookTag.of(this, tag));
    }
    /**
     * 태그를 제거합니다.
     */
    public void removeTag(Tag tag) {
        tags.removeIf(link -> link.getTag().equals(tag));
    }

}
