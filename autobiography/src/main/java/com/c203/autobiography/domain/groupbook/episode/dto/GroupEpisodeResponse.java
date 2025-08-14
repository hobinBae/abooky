package com.c203.autobiography.domain.groupbook.episode.dto;

import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupEpisodeResponse {
    private final Long id;
    private final Long groupBookId;
    private final String title;
    private final Integer orderNo;
    private final String status;
    private final Integer currentStep;
    private final String editedContent;

    // 첫 질문을 위한 필드 추가
    private final String currentQuestion;
    // 🎯 다음 에피소드 관련 필드들
    private final String nextEpisodeQuestion;     // 다음 에피소드 첫 질문
    private final String nextQuestionKey;         // 다음 질문 키
    private final String recommendedTemplate;     // 추천 템플릿
    private final String recommendedTitle;        // 추천 제목

    public static GroupEpisodeResponse of(GroupEpisode ep) {
        return GroupEpisodeResponse.builder()
                .id(ep.getGroupEpisodeId())
                .groupBookId(ep.getGroupBook().getGroupBookId())
                .title(ep.getTitle())
                .orderNo(ep.getOrderNo())
                .status(ep.getStatus().name())
                .currentStep(ep.getCurrentStep())
                .editedContent(ep.getEditedContent())
                .build();
    }
}
