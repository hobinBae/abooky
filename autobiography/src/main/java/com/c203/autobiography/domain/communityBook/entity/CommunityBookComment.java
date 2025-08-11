package com.c203.autobiography.domain.communityBook.entity;

// JPA 표준 API

import com.c203.autobiography.domain.book.entity.BookCategory;
import com.c203.autobiography.domain.communityBook.dto.CommunityBookType;
import com.c203.autobiography.domain.member.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "community_book_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
// 논리 삭제 적용
@SQLDelete(sql = "UPDATE community_book_comment SET deleted_at = CURRENT_TIMESTAMP WHERE comment_id = ?")
@Where(clause = "deleted_at IS NULL")
public class CommunityBookComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_book_comment_id")
    private Long communityBookCommentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "community_book_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_community_book_comment_book")
    )
    private CommunityBook communityBook;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_community_book_comment_member")
    )
    private Member member;


    @NotBlank
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // — 도메인 행위 메서드 —

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
