package com.c203.autobiography.domain.episode.entity;

import com.c203.autobiography.domain.book.Entity.Book;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "episode")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLDelete(sql = "UPDATE episode SET deleted_at = CURRENT_TIMESTAMP WHERE episode_id = ?")
@Where(clause = "deleted_at IS NULL")
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "episode_id")
    private Long episodeId;

    // Book과 N:1 연관관계, Lazy 로딩, NOT NULL 제약
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "book_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_episode_book")
    )
    private Book book;

    //테스트 코드
    @Builder.Default
    @Column(nullable = false, length = 100)
    private String title = "테스트";

    @Column(name = "episode_date")
    private LocalDate episodeDate;

    @Column(name = "episode_order")
    private Integer episodeOrder;

    @Lob
    private String content;

    @Column(name = "audio_url")
    private String audioUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 태그 추가
     *
     */
    @OneToMany(
            mappedBy = "episode",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<EpisodeTag> tags = new ArrayList<>();

    // — 도메인 메서드 추가 —
    /**
     * 에피소드의 주요 내용을 한 번에 갱신합니다.
     */
    public void updateEpisode(
            String title,
            LocalDate episodeDate,
            Integer episodeOrder,
            String content,
            String audioUrl
    ) {
        this.title = title;
        this.episodeDate = episodeDate;
        this.episodeOrder = episodeOrder;
        this.content = content;
        this.audioUrl = audioUrl;
    }
    /**
     * 삭제 요청 시 deletedAt에 타임스탬프를 설정합니다.
     * (영구삭제 대신 논리삭제를 사용)
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 태그를 추가합니다.
     */
    public void addTag(Tag tag) {
        EpisodeTag link = EpisodeTag.of(this, tag);
        tags.add(link);
    }
    /**
     * 태그를 제거합니다.
     */
    public void removeTag(Tag tag) {
        tags.removeIf(link -> link.getTag().equals(tag));
    }
}
