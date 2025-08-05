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
    private int requestTimeoutSec = 30;
    private int maxTokensFollowup  = 50;
    private int maxTokensNext      = 100;
    private double temperature     = 0.7;
    // 프롬프트 템플릿
    private String followupSystem = "당신은 자서전을 작성하는 친절한 인터뷰어입니다. " +
            "사용자의 답변을 듣고, 추가 질문을 통해 더 깊이 있는 에피소드를 이끌어낼 수 있다고 판단되면, " +
            "자연스럽고 문맥에 맞는 한 문장의 후속 질문을 생성해주세요. " +
            "만약 없다면 질문을 하지 않아도 괜찮습니다.";
    private String followupUserTemplate = "사용자가 이렇게 답했습니다:\n\"%s\"\n후속 질문을 한 문장으로 만들어 주세요.";
    private String nextQUserTemplate = "방금 제 답변은 이렇습니다: \"%s\". 이제 이어질 다음 인터뷰 질문을 한 문장으로 제안해주세요.";
    
    // 챕터 기반 동적 후속질문용 프롬프트 - 한국어 이름 인식 강화
    private String dynamicFollowUpSystem = "당신은 자서전을 작성하는 전문 인터뷰어입니다. " +
            "주어진 단계와 맥락에 맞춰 사용자의 답변을 바탕으로 깊이 있는 후속 질문 2-3개를 생성해주세요. " +
            "각 질문은 한 줄씩 번호 없이 작성해주세요. " +
            "중요: 한국어 이름을 정확히 인식하세요. '제 이름은 배우빈' 같은 경우 '배우빈'은 하나의 이름이지 직업이 아닙니다. " +
            "사용자가 명시적으로 언급한 정보만을 바탕으로 질문하고, 추측이나 가정은 하지 마세요.";
    
    // 에피소드 생성용 프롬프트
    private String episodeGenerationSystem = "당신은 자서전 편집 전문가입니다. " +
            "제공된 대화 내용을 바탕으로 자연스럽고 감동적인 자서전 에피소드를 작성해주세요. " +
            "일인칭 시점으로 작성하며, 감정과 디테일을 살려주세요.";
    private String episodeGenerationTemplate = "다음 인터뷰 내용을 바탕으로 자서전 에피소드를 작성해주세요:\n\n%s\n\n" +
            "위 내용을 자연스러운 자서전 형태로 정리해주세요.";
    
    // 답변 분석용 프롬프트 - 정확도 개선
    private String answerAnalysisSystem = "당신은 인터뷰 답변 분석 전문가입니다. " +
            "사용자의 답변을 꼼꼼히 분석하여 이미 충분히 답변된 질문들을 정확히 식별해주세요. " +
            "중요한 것은 사용자가 이미 언급한 내용에 대해서는 중복으로 묻지 않는 것입니다. " +
            "아직 전혀 언급되지 않았거나 더 자세한 설명이 필요한 질문의 번호만 쉼표로 구분하여 반환하세요 (예: 2,4). " +
            "모든 질문이 충분히 답변되었다면 반드시 'NONE'을 반환하세요.";
    private String answerAnalysisTemplate = "사용자 답변: \"%s\"\n\n후속 질문 목록:\n%s\n\n" +
            "위 답변을 바탕으로 판단해주세요:\n" +
            "- 이미 충분히 답변된 질문은 제외\n" +
            "- 아직 언급되지 않은 질문만 번호로 표시\n" +
            "- 모든 질문이 답변되었으면 'NONE' 응답\n\n" +
            "답변:";
}
