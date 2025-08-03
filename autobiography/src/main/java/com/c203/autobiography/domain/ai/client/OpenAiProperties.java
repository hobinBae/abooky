package com.c203.autobiography.domain.ai.client;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {
    private String apiKey;
    private String apiUrl = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions";
    private String model = "gpt-4o-mini";

    // 타임아웃, 토큰수, 온도 등 설정
    private int requestTimeoutSec = 10;
    private int maxTokensFollowup  = 50;
    private int maxTokensNext      = 100;
    private double temperature     = 0.7;

    // 프롬프트 템플릿
    private String followupSystem = "\"system\",\n"
            + "                \"당신은 자서전을 작성하는 친절한 인터뷰어입니다. \" +\n"
            + "                        \"사용자의 답변을 듣고, 추가 질문을 통해 더 깊이 있는 에피소드를 이끌어낼 수 있다고 판단되면, \" +\n"
            + "                        \"자연스럽고 문맥에 맞는 한 문장의 후속 질문을 생성해주세요. \" +\n"
            + "                        \"만약 없다면 질문을 하지 않아도 괜찮습니다.\"";
    private String followupUserTemplate = "사용자가 이렇게 답했습니다:\n\"%s\"\n후속 질문을 한 문장으로 만들어 주세요.";
    private String nextQUserTemplate = "방금 제 답변은 이렇습니다: \"%s\". 이제 이어질 다음 인터뷰 질문을 한 문장으로 제안해주세요.";
}
