package com.c203.autobiography.domain.communityBook.entity;

// JPA 표준 API
import com.c203.autobiography.domain.book.dto.BookType;
import com.c203.autobiography.domain.book.entity.BookCategory;
import com.c203.autobiography.domain.member.entity.Member;
import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

// Hibernate 전용 기능
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_book")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
// 논리 삭제 적용
@SQLDelete(sql = "UPDATE community_book SET deleted_at = CURRENT_TIMESTAMP WHERE community_book_id = ?")
@Where(clause = "deleted_at IS NULL")
public class CommunityBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_book_id")
    private Long communityBookId;

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
            foreignKey = @ForeignKey(name = "fk_community_book_member")
    )
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_type", nullable = false, length = 20)
    @Builder.Default
    private BookType bookType = BookType.FREE_FORM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "category_id",
            foreignKey = @ForeignKey(name = "fk_community_book_category")
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
    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Builder.Default
    @Column(
            name = "average_rating",
            precision = 2,
            scale = 1,
            nullable = false
    )
    private BigDecimal averageRating = BigDecimal.valueOf(0.0);

    // — 도메인 행위 메서드 —

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
