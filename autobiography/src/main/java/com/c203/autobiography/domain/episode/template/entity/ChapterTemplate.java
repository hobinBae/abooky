package com.c203.autobiography.domain.episode.template.entity;

import com.c203.autobiography.domain.episode.template.dto.FollowUpType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "chapter_template",
        uniqueConstraints = {
                // 같은 챕터 안에서 template_order가 1개만 존재
                @UniqueConstraint(name = "uk_ct__chapter_template_order", columnNames = {"chapter_id","template_order"}),
                // (선택) 같은 챕터 안에서 stage_level 유일 보장
                @UniqueConstraint(name = "uk_ct__chapter_stage_level", columnNames = {"chapter_id","stage_level"})
        },
        indexes = {
                @Index(name = "idx_ct__chapter_order", columnList = "chapter_id, template_order"),
                @Index(name = "idx_ct__chapter_stage", columnList = "chapter_id, stage_level")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChapterTemplate {
    
    @Id
    @Column(name = "template_id")
    private String templateId;
    
    @Column(name = "stage_name", nullable = false)
    private String stageName;
    
    @Column(name = "main_question", columnDefinition = "TEXT", nullable = false)
    private String mainQuestion;
    
    @Column(name = "template_order", nullable = false)
    private Integer templateOrder;

    /** 연대기 흐름을 위한 절대 단계 값 (1,2,3…) */
    @Column(name = "stage_level", nullable = false)
    private Integer stageLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "followup_type", nullable = false)
    private FollowUpType followUpType;
    
    @Column(name = "dynamic_prompt_template", columnDefinition = "TEXT")
    private String dynamicPromptTemplate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;
    
    @OneToMany(mappedBy = "chapterTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FollowUpQuestion> staticFollowUpQuestions;
    
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

