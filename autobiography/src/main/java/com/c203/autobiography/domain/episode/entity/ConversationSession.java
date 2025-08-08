package com.c203.autobiography.domain.episode.entity;

import com.c203.autobiography.domain.episode.dto.SessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Deque;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conversation_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class ConversationSession {
    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "episode_id", nullable = true)
    private Long episodeId;

    // 진행 중인 템플릿 인덱스 추가 (0부터 시작) - 레거시
    @Builder.Default
    @Column(name = "template_index", nullable = false)
    private Integer templateIndex = 0;
    
    // 챕터 기반 진행 상태 추가
    @Column(name = "current_chapter_id")
    private String currentChapterId;
    
    @Column(name = "current_template_id")
    private String currentTemplateId;
    
    @Builder.Default
    @Column(name = "followup_question_index", nullable = false)
    private Integer followUpQuestionIndex = 0;
    
    @Builder.Default
    @Column(name = "current_chapter_order", nullable = false)
    private Integer currentChapterOrder = 1;
    
    @Builder.Default
    @Column(name = "current_template_order", nullable = false)
    private Integer currentTemplateOrder = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status;

    @Column(name = "token_count", nullable = false)
    private Long tokenCount;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
