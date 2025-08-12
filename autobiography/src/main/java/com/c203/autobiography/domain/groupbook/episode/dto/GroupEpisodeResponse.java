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
