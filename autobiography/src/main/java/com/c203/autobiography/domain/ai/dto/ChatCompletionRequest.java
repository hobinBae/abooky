package com.c203.autobiography.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private Double temperature;

}
