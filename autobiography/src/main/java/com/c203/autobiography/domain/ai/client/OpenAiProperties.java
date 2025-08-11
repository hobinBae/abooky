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
    private int maxTokensFollowup = 50; // 후속 질문 생성 api 호출 시, 최대 생성 토큰 개수
    private int maxTokensNext = 150;
    private double temperature = 0.7; // 생성 텍스트의 무작위성(창의성) 정도를 제어하는 하이퍼파라미터(0.0 ~ 1.0)
    // 프롬프트 템플릿
    private String followupSystem = "당신은 자서전 인터뷰어입니다. 목표는 사용자의 답변을 바탕으로 깊이 있는 후속 질문을 만드는 것입니다.\n" +
            "규칙:\n" +
            "1) 사용자가 실제로 말한 사실만 사용, 추측/가정/과장은 금지\n" +
            "2) 이름·지명 등 고유명사를 오해하지 말 것(철자 보정 금지)\n" +
            "3) 중복/유사 질문 금지, 한 질문엔 한 주제만 다룰 것\n" +
            "4) 민감하거나 트라우마일 수 있는 주제는 배려적인 톤으로 질문(선택권 제공)\n" +
            "5) 존댓말 사용, 질문은 한 줄로 간결하게\n" +
            "6) 출력은 질문만, 불릿·번호·따옴표 없이 줄바꿈으로 구분";

    // 2) 섹션별 동적 후속질문 user 템플릿
    private String dynUserIntro = "사용자가 자신을 이렇게 소개했습니다: \"%s\"\n" +
            "위 소개에서 실제로 언급된 사람/장소/사건/역할 중 하나를 골라 그 감정·의미·배경을 더 알 수 있는 후속 질문 2~3개를 작성하세요.\n" +
            "규칙: 추측 금지, 한 질문엔 한 주제, 중복 금지, 존댓말, 각 질문은 한 줄, " +
            "질문만 출력(불릿/번호/따옴표/설명 금지). 질문들은 줄바꿈으로만 구분하세요.";

    /**
     * INTRO_SCENE: ‘나를 보여주는 하루 한 장면’ 후속 질문 템플릿
     */
    private String dynUserIntroScene =
            "사용자가 자신의 정체성을 보여주는 장면을 이렇게 묘사했습니다: \"%s\"\n" +
                    "그 장면에서 실제로 언급된 시간/장소/인물/행동 중 하나를 선택하여, " +
                    "감각(소리/냄새/빛/온도), 당시 감정의 변화, 그 장면이 갖는 의미를 더 구체화할 후속 질문 2~3개를 작성하세요.\n" +
                    "규칙: 추측 금지, 한 질문엔 한 주제, 중복 금지, 존댓말, 각 질문은 한 줄, " +
                    "질문만 출력(불릿/번호/따옴표/설명 금지). 질문들은 줄바꿈으로만 구분하세요.";

    // ✅ 집/동네/일상 환경(공간·리듬·규칙)
    private String dynUserChildhoodHome =
            "단계: 유년기(집/동네/일상 환경)\n사용자의 답변: \"%s\"\n" +
                    "사용자가 실제로 언급한 공간(집/방/골목/학교까지 길), 하루 리듬(식사/잠/놀이/학원), " +
                    "집안의 소리·빛·냄새·온도 또는 가족 규칙 중 하나를 골라, 그 배경과 감정/의미를 더 구체화하는 후속 질문 2~3개를 작성하세요. " +
                    "추측 금지, 존댓말, 각 질문은 한 줄, 질문만 출력, 줄바꿈으로 구분하세요.";

    // ✅ 돌봄자/가족 역할(말투·관계·가치의 씨앗)
    private String dynUserChildhoodCaregiver =
            "단계: 유년기(돌봄자/가족 역할)\n사용자의 답변: \"%s\"\n" +
                    "돌봄자(부모/조부모/형제/기타)와의 상호작용에서 실제로 언급된 말투/자주 하던 말/몸짓/습관, " +
                    "가족 내 역할 중 하나를 선택해 그것이 사용자의 감정/의사결정/가치관에 남긴 흔적을 탐구하는 질문 2~3개를 작성하세요. " +
                    "배려적 톤, 추측 금지, 각 질문 한 줄, 질문만 출력, 줄바꿈으로 구분하세요.";


    // ✅ 생생한 한 장면(감각·감정·의미 고도화)
    private String dynUserChildhoodScene =
            "단계: 유년기(생생한 한 장면)\n사용자의 답변: \"%s\"\n" +
                    "장면 속 시간/장소/인물/행동 중 실제 언급된 요소 하나를 택해, 감각(소리/냄새/빛/온도/촉감), " +
                    "그 순간의 감정 변화, 장면이 갖는 의미 또는 이후 선택에 미친 영향 등을 더 깊게 묻는 질문 2~3개를 작성하세요. " +
                    "추측 금지, 각 질문 한 줄, 질문만 출력, 줄바꿈으로 구분하세요.";

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
    private String dynamicFollowUpSystem = "당신은 자서전 인터뷰 전문가입니다. " +
            "규칙: 1) 사용자가 실제로 언급한 사실만 사용 2) 추측/가정 금지 3) 한 질문엔 한 주제 4) 존댓말 5) 감정/의미/배경을 깊이 탐구 " +
            "6) 질문은 1~2문장, 불필요한 설명 금지.";


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
