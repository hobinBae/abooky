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
public class ChatCompletionResponse {
    private String id;
    private String object;
    private List<Choice> choices;
    private Usage usage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice{
        private int index;
        private ChatMessage message;
        private String finish_reason;
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
