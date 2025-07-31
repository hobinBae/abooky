package com.c203.autobiography.domain.ai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatCompletionRequest {
    private String model;
    private List<ChatMessage> messages;
    private Integer maxTokens;
    private Double temperature;

}
