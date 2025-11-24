package com.c203.autobiography.domain.ai.prompt;

import java.util.Map;
import lombok.Data;

@Data
public class EditPrompts {
    private String system;
    private Integer maxTokens;
    private Double temperature;

    // toneType → tonePrompt
    private Map<String, String> tones;
}
