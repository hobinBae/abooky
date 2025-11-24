package com.c203.autobiography.domain.ai.prompt;

import lombok.Data;

@Data
public class AnalysisPrompts {
    private String system;
    private String template;
    private Integer maxTokens;
    private Double temperature;
}
