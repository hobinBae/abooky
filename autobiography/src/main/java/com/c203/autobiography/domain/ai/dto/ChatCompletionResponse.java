package com.c203.autobiography.domain.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionResponse {
    private String id;
    private String object;
    private List<Choice> choices;
    private Usage usage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice{
        private int index;
//        private ChatMessage message;
        private MessageResponse message;
        private String finish_reason;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageResponse {
        private String content; // 우리가 최종적으로 필요한 내용이 담긴 JSON 문자열
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage{
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }

}
