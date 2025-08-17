package com.c203.autobiography.domain.groupbook.entity;

import com.c203.autobiography.domain.member.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_book_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
// 논리 삭제 적용
@SQLDelete(sql = "UPDATE group_book_comment SET deleted_at = CURRENT_TIMESTAMP WHERE group_book_comment_id = ?")
@Where(clause = "deleted_at IS NULL")
public class GroupBookComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_book_comment_id")
    private Long groupBookCommentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "group_book_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_group_book_comment_book")
    )
    private GroupBook groupBook;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_group_book_comment_member")
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

    /**
     * 댓글 내용 수정
     */
    public void updateContent(String newContent) {
        this.content = newContent;
    }

    /**
     * 댓글 작성자인지 확인
     */
    public boolean isWrittenBy(Long memberId) {
        return this.member.getMemberId().equals(memberId);
    }
}