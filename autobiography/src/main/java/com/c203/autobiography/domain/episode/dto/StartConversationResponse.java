package com.c203.autobiography.domain.episode.dto;

import com.c203.autobiography.domain.episode.template.dto.NextQuestionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartConversationResponse {

    private String sessionId;
    private NextQuestionDto firstQuestion;
}
