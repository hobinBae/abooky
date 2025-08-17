package com.c203.autobiography.domain.communityBook.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.*;

@Entity
@Table(name = "community_episode")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
// 논리 삭제 적용
@SQLDelete(sql = "UPDATE community_book_episode SET deleted_at = CURRENT_TIMESTAMP WHERE community_episode_id = ?")
@Where(clause = "deleted_at IS NULL")
public class CommunityBookEpisode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_episode_id")
    private Long communityEpisodeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "community_book_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_community_episode_book")
    )
    private CommunityBook communityBook;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "episode_date")
    private LocalDate episodeDate;

    @Column(name = "episode_order")
    private Integer episodeOrder;

    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Size(max = 255)
    @Column(name = "audio_url", length = 255)
    private String audioUrl;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
