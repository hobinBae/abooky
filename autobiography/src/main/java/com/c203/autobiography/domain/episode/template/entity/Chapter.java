package com.c203.autobiography.domain.episode.template.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chapter")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Chapter {
    
    @Id
    @Column(name = "chapter_id")
    private String chapterId;
    
    @Column(name = "chapter_name", nullable = false)
    private String chapterName;
    
    @Column(name = "chapter_order", nullable = false)
    private Integer chapterOrder;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChapterTemplate> templates;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
} 