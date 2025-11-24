package com.c203.autobiography.domain.ai.properties;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    private String apiKey;
    private String apiUrl = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions";
    private String model = "gpt-4o";
    private int requestTimeoutSec = 60;

    private Api api = new Api();

    @Data
    public static class Api {
        private String apiKey;
        private String apiUrl;
        private String model;
        private int requestTimeoutSec = 60;
    }


    // ------------------------
    // FOLLOW-UP 설정
    // ------------------------
    private FollowUp followup = new FollowUp();

    @Data
    public static class FollowUp {
        private String system;
        private int maxTokens = 300;
        private double temperature = 0.7;

        // PROMPT_KEY → Prompt(system, template)
        private Map<String, Prompt> prompts = new HashMap<>();
    }

    // ------------------------
    // EPISODE 설정
    // ------------------------
    private Episode episode = new Episode();

    @Data
    public static class Episode {
        private int maxTokens = 1500;
        private double temperature = 0.8;

        // chapterId → Prompt
        private Map<String, Prompt> prompts = new HashMap<>();
    }

    // ------------------------
    // ANSWER ANALYSIS 설정
    // ------------------------
    private Analysis analysis = new Analysis();

    @Data
    public static class Analysis {
        private String system;
        private String template;
        private int maxTokens = 50;
        private double temperature = 0.1;
    }

    // ------------------------
    // PROOFREAD 설정
    // ------------------------
    private Proofread proofread = new Proofread();

    @Data
    public static class Proofread {
        private String system;
        private String template;
        private int maxTokens = 1500;
        private double temperature = 0.3;
    }

    // ------------------------
    // TEXT EDIT 설정
    // ------------------------
    private Edit edit = new Edit();

    @Data
    public static class Edit {
        private String system;
        private int maxTokens = 300;
        private double temperature = 0.3;

        // tone 이름 → tone template
        private Map<String, String> tones = new HashMap<>();
    }

    // ------------------------
    // 공통 Prompt 구조
    // ------------------------
    @Data
    public static class Prompt {
        private String system;
        private String userTemplate;
    }


}
