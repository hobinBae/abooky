package com.c203.autobiography.domain.groupbook.episode.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StepNextResponse {
    private final Integer stepNo;
    private final String question;
    private final String editedParagraph;
    private final boolean completed;
}
