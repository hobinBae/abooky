package com.c203.autobiography.domain.groupbook.episode.dto;

import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 JSON에서 제외
public class GroupEpisodeResponse {
    private final Long id;
    private final Long groupBookId;
    private final String title;
    private final String summary;
    private final Integer orderNo;
    private final String status;
    private final Integer currentStep;
    private final String editedContent;
    private final String template;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // 선택적 필드들 (필요할 때만 포함)
    private final String currentQuestion;
    private final String nextEpisodeQuestion;
    private final String nextQuestionKey;
    private final String recommendedTemplate;
    private final String recommendedTitle;

    // 기본 생성용 정적 메서드
    public static GroupEpisodeResponse of(GroupEpisode ep) {
        return GroupEpisodeResponse.builder()
                .id(ep.getGroupEpisodeId())
                .groupBookId(ep.getGroupBook().getGroupBookId())
                .title(ep.getTitle())
                .orderNo(ep.getOrderNo())
                .status(ep.getStatus().name())
                .currentStep(ep.getCurrentStep())
                .editedContent(ep.getEditedContent())
                .template(ep.getTemplate())
                .createdAt(ep.getCreatedAt())
                .updatedAt(ep.getUpdatedAt())
                .build();
    }

    // 질문 정보 포함용 정적 메서드
    public static GroupEpisodeResponse withQuestion(GroupEpisode ep, String currentQuestion) {
        return GroupEpisodeResponse.builder()
                .id(ep.getGroupEpisodeId())
                .groupBookId(ep.getGroupBook().getGroupBookId())
                .title(ep.getTitle())
                .orderNo(ep.getOrderNo())
                .status(ep.getStatus().name())
                .currentStep(ep.getCurrentStep())
                .editedContent(ep.getEditedContent())
                .template(ep.getTemplate())
                .createdAt(ep.getCreatedAt())
                .updatedAt(ep.getUpdatedAt())
                .currentQuestion(currentQuestion)
                .build();
    }

    // 다음 에피소드 추천 정보 포함용 정적 메서드
    public static GroupEpisodeResponse withNextRecommendation(GroupEpisode ep, 
                                                            String nextQuestion, 
                                                            String nextQuestionKey,
                                                            String recommendedTemplate,
                                                            String recommendedTitle) {
        return GroupEpisodeResponse.builder()
                .id(ep.getGroupEpisodeId())
                .groupBookId(ep.getGroupBook().getGroupBookId())
                .title(ep.getTitle())
                .orderNo(ep.getOrderNo())
                .status(ep.getStatus().name())
                .currentStep(ep.getCurrentStep())
                .editedContent(ep.getEditedContent())
                .template(ep.getTemplate())
                .createdAt(ep.getCreatedAt())
                .updatedAt(ep.getUpdatedAt())
                .nextEpisodeQuestion(nextQuestion)
                .nextQuestionKey(nextQuestionKey)
                .recommendedTemplate(recommendedTemplate)
                .recommendedTitle(recommendedTitle)
                .build();
    }
}
