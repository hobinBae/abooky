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
    private int maxTokensFollowup  = 50; // 후속 질문 생성 api 호출 시, 최대 생성 토큰 개수
    private int maxTokensNext      = 150;
    private double temperature     = 0.7; // 생성 텍스트의 무작위성(창의성) 정도를 제어하는 하이퍼파라미터(0.0 ~ 1.0)
    // 프롬프트 템플릿
    private String followupSystem = "당신은 자서전을 작성하는 전문 인터뷰어입니다. " +
            "목적: 사용자가 실제로 언급한 사실과 감정에 기반해, 한 단계씩 깊이를 더하는 2~3개의 후속 질문을 생성합니다. " +
            "원칙: " +
            "1) 한국어 이름을 정확히 인식하고, 이름·지명 등 고유명사를 오해하지 마세요. " +
            "2) 사용자가 명시적으로 말한 내용만 바탕으로 질문하세요. 추측·과장·가정을 하지 마세요. " +
            "3) 중복 질문을 피하고, 감정/의미/전환점/맥락을 깊이 파고드세요. " +
            "4) 질문은 한 줄씩, 번호·불릿 없이 간결하게 작성하세요. " +
            "5) 민감 주제일 경우 배려하고 중립적인 톤을 유지하세요.";

    // 2) 섹션별 동적 후속질문 user 템플릿
    private String dynUserIntro =
            "단계: 시작/사용자 기본정보\n사용자의 답변: \"%s\"\n" +
                    "위 답변에서 더 깊게 탐구할 가치가 있는 대목을 중심으로, 추측 없이 2~3개의 후속 질문을 작성하세요.";

    private String dynUserChildhood =
            "단계: 유년기\n사용자의 답변: \"%s\"\n" +
                    "사용자가 언급한 사건/인물/장소 중 감정과 의미가 드러나는 지점을 바탕으로, 추측 없이 2~3개의 후속 질문을 작성하세요.";

    private String dynUserUpperElem =
            "단계: 아동기 후반~초등 고학년\n사용자의 답변: \"%s\"\n" +
                    "사용자가 밝힌 관계/감정/전환 신호를 더 깊이 탐구할 2~3개의 질문을 작성하세요. 추측 금지.";

    private String dynUserMiddle =
            "단계: 중학교\n사용자의 답변: \"%s\"\n" +
                    "정체성/관계/감정의 변곡점을 더 구체화하는 2~3개의 질문을 작성하세요. 사용자가 말한 사실에만 근거하세요.";

    private String dynUserHigh =
            "단계: 고등학교\n사용자의 답변: \"%s\"\n" +
                    "동기/압력/회복탄력성/가치 재정의를 파고드는 2~3개의 질문을 작성하세요. 추측 없이.";

    private String dynUserTransition =
            "단계: 졸업 전환기\n사용자의 답변: \"%s\"\n" +
                    "선택의 근거/감정 곡선/배운 점을 구체화하는 2~3개의 질문을 작성하세요. 추측 금지.";

    private String dynUserCollegeOrWork =
            "단계: 대학/사회 진입 초기\n사용자의 답변: \"%s\"\n" +
                    "적응/관계/자기효능감 변화를 깊이 파고드는 2~3개의 질문을 작성하세요. 추측 없이.";


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
    
//    // 답변 분석용 프롬프트 - 정확도 개선
//    private String answerAnalysisSystem = "당신은 인터뷰 답변 분석 전문가입니다. " +
//            "사용자의 답변을 꼼꼼히 분석하여 이미 충분히 답변된 질문들을 정확히 식별해주세요. " +
//            "중요한 것은 사용자가 이미 언급한 내용에 대해서는 중복으로 묻지 않는 것입니다. " +
//            "아직 전혀 언급되지 않았거나 더 자세한 설명이 필요한 질문의 번호만 쉼표로 구분하여 반환하세요 (예: 2,4). " +
//            "모든 질문이 충분히 답변되었다면 반드시 'NONE'을 반환하세요.";

    private String answerAnalysisSystem =
            "당신은 인터뷰 답변 분석가입니다. 사용자의 답변이 아래 정적 후속질문에 이미 충분히 답했는지 판별하세요. " +
                    "충분히 답한 질문은 제외하고, 아직 언급되지 않았거나 더 필요한 질문의 번호만 쉼표로 반환하세요. 모두 커버되면 'NONE'. " +
                    "판단은 사용자가 실제로 언급한 내용에만 근거하세요.";

    private String answerAnalysisTemplate =
            "사용자 답변: \"%s\"\n\n후속 질문 목록:\n%s\n\n" +
                    "규칙:\n- 이미 충분히 답한 질문 제외\n- 아직 언급 안 된 질문 번호만 쉼표로\n- 모두 커버되면 'NONE'\n답변:";
}
