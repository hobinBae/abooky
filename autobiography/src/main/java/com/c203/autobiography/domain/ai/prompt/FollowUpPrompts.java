package com.c203.autobiography.domain.ai.prompt;

import java.util.Map;
import lombok.Data;

@Data
public class FollowUpPrompts {
    private String system;
    private Integer maxTokens;
    private Double temperature;

    private Map<String, PromptEntry> prompts;

    @Data
    public static class PromptEntry {
        private String system;
        private String userTemplate;
    }
}
