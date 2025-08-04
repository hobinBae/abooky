package com.c203.autobiography.domain.episode.template.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "question_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuestionTemplate {
    @Id
    @Column(name = "template_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;

    @Column(name = "template_text", columnDefinition = "TEXT", nullable = false)
    @Setter                             // 템플릿 내용은 변경 가능하다면 허용
    private String templateText;

    @Column(name = "template_order", nullable = false)
    @Setter                             // 순서 변경이 필요할 수도 있으니 허용
    private Integer templateOrder;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;    // 생성 시에만 설정 → setter 제거





}
