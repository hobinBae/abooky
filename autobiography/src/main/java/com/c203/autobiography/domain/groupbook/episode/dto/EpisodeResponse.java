package com.c203.autobiography.domain.groupbook.episode.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EpisodeResponse {
    private final Long id;
    private final Long groupBookId;
    private final String title;
    private final Integer orderNo;
    private final String status;
    private final Integer currentStep;
    private final String editedContent;
}
