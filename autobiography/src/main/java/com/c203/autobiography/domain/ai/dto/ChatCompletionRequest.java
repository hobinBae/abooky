package com.c203.autobiography.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@Builder
public class ChatCompletionRequest {
    private String model;
    private List<ChatMessage> messages;
    @JsonProperty("response_format") // 직렬화 시 필드 이름을 "response_format"으로 지정
    private ResponseFormat responseFormat;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private Double temperature;

    @Getter
    @AllArgsConstructor
    public static class ResponseFormat {
        private String type;
    }
}
