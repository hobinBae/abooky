package com.c203.autobiography.domain.episode.template.entity;

import com.c203.autobiography.domain.episode.template.dto.FollowUpType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chapter_template")
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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "followup_type", nullable = false)
    private FollowUpType followUpType;
    
    @Column(name = "dynamic_prompt_template", columnDefinition = "TEXT")
    private String dynamicPromptTemplate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;
    
    @OneToMany(mappedBy = "chapterTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FollowUpQuestion> staticFollowUpQuestions;
    
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

