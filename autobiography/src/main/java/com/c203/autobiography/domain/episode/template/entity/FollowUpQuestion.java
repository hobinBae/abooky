package com.c203.autobiography.domain.episode.template.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "followup_question",
        uniqueConstraints = {
                // 같은 템플릿 안에서 question_order 유일
                @UniqueConstraint(name = "uk_fu__template_question_order", columnNames = {"template_id","question_order"})
        },
        indexes = {
                @Index(name = "idx_fu__template_order", columnList = "template_id, question_order")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FollowUpQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "followup_id")
    private Long followUpId;
    
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;
    
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ChapterTemplate chapterTemplate;
    
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
} 