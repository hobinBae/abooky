package com.c203.autobiography.domain.groupbook.episode.entity;

import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_episode")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GroupEpisode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_episode_id")
    private Long groupEpisodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_book_id", insertable = false, updatable = false)
    private GroupBook groupBook;

    @Column(name ="title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_no")
    private Integer orderNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EpisodeStatus status;

    @Lob
    @Column(name = "raw_notes")
    private String rawNotes; // 사용자 원문 누적(선택)

    @Lob
    @Column(name = "edited_content")
    private String editedContent; // 교정/편집 누적

    @Column(name = "current_step")
    private Integer currentStep; // 가이드 모드용

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static GroupEpisode toEntity(GroupBook gb, String title, Integer orderNo) {
        LocalDateTime now = LocalDateTime.now();
        return GroupEpisode.builder()
                .groupBook(gb)
                .title(title)
                .orderNo(orderNo)
                .status(EpisodeStatus.DRAFT)
                .rawNotes("")
                .editedContent("")
                .currentStep(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void touch() { this.updatedAt = LocalDateTime.now(); }

    public void setStatus(EpisodeStatus s) { this.status = s; touch(); }

    public void appendEdited(String paragraph) {
        this.editedContent = (this.editedContent == null || this.editedContent.isBlank())
                ? paragraph
                : this.editedContent + "\n\n" + paragraph;
        touch();
    }

    public void nextStep() { this.currentStep = (this.currentStep == null ? 0 : this.currentStep) + 1; touch(); }


    public void softDelete() { this.deletedAt = LocalDateTime.now(); }
}
