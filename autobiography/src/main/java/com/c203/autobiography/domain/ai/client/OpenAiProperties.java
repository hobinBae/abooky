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

    private String dynamicFollowUpSystem = "당신은 따뜻하고 공감 능력이 뛰어난 자서전 인터뷰 전문가입니다. " +
            "사용자의 답변을 바탕으로, 그들의 삶의 한 조각을 더 깊이 이해하기 위한 후속 질문을 생성합니다." +
            "규칙:" +
            " 1) 사용자가 실제로 언급한 사실에만 근거하여 질문합니다." +
            "2) 추측, 가정, 섣부른 판단을 하지 않습니다. " +
            "3) 하나의 질문에는 하나의 주제만 담습니다. " +
            "4) 항상 존댓말을 사용하며, 질문은 1~2개의 문장으로 간결하게 표현합니다. " +
            "5) 사용자의 감정, 생각의 변화, 사건의 의미를 더 깊이 탐색하는 것을 목표로 합니다. " +
            "6) 모든 출력은 오직 질문 텍스트만 포함하며, 번호나 따옴표 없이 각 질문은 줄바꿈으로만 구분합니다.";

    // --- 프롤로그 (1챕터) 프롬프트 ---
    private String dynUserIntro = "단계: 프롤로그(자기소개)\n사용자 답변: \"%s\"\n\n위 소개에서 사용자가 언급한 호칭, 역할, 출생 배경, 혹은 기록의 이유 중 하나를 선택하여, 그 의미나 감정을 더 깊이 탐색하는 후속 질문 2~3개를 작성하세요.";
    private String dynUserIntroScene = "단계: 프롤로그(대표 장면)\n사용자 답변: \"%s\"\n\n사용자가 묘사한 '오늘의 한 장면' 속에서, 언급된 감각(소리, 냄새 등), 감정, 행동, 혹은 장소 중 하나를 골라 그 순간의 의미를 더 구체화하는 후속 질문 2~3개를 작성하세요.";

    // --- 유년기 (2챕터) 프롬프트 ---
    private String dynUserChildhoodHome = "단계: 유년기(집과 동네)\n사용자 답변: \"%s\"\n\n언급된 집이나 동네의 감각적 기억(소리, 빛, 냄새 등)과 관련하여, 그 안에서 느꼈던 안정감이나 특별한 추억에 대해 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserChildhoodCaregiver = "단계: 유년기(나를 키운 사람)\n사용자 답변: \"%s\"\n\n언급된 '돌봐주신 분'의 특정 행동이나 말투가 당시 사용자에게 어떤 감정을 느끼게 했는지, 그리고 어떤 영향을 주었는지를 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserChildhoodPersonality = "단계: 유년기(나의 성격)\n사용자 답변: \"%s\"\n\n사용자가 묘사한 자신의 어린 시절 성격이 잘 드러났던 구체적인 사건이나, 그 성격 때문에 겪었던 재미있는 일화에 대해 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserChildhoodPlay = "단계: 유년기(놀이와 장난감)\n사용자 답변: \"%s\"\n\n언급된 놀이나 장난감, 동화책이 왜 그렇게 좋았는지, 그것과 관련된 가장 소중한 추억은 무엇인지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserChildhoodFavorites = "단계: 유년기(나의 우상)\n사용자 답변: \"%s\"\n\n언급된 '최애' 캐릭터를 얼마나 좋아했는지 보여주는 행동(따라하기, 물건 수집 등)이 있었는지, 그 대상이 지금의 나에게 어떤 흔적을 남겼는지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserChildhoodDreamJob = "단계: 유년기(장래희망)\n사용자 답변: \"%s\"\n\n언급된 장래희망을 갖게 된 구체적인 계기가 무엇이었는지, 그 꿈을 위해 어떤 상상을 했었는지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserChildhoodScene = "단계: 유년기(선명한 한 장면)\n사용자 답변: \"%s\"\n\n묘사된 장면 속에서 느꼈던 감정의 변화나, 그 사건이 끝난 뒤 마음에 남았던 생각에 대해 더 깊이 묻는 후속 질문 2~3개를 작성하세요.";

    // --- 초등 고학년 (3챕터) 프롬프트 ---
    private String dynUserUpperElemSchool = "단계: 초등 고학년(학교)\n사용자 답변: \"%s\"\n\n묘사된 학교의 풍경이나 감각 속에서, 사용자의 감정이나 친구와의 관계가 드러나는 구체적인 사건에 대해 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserUpperElemPeople = "단계: 초등 고학년(친구)\n사용자 답변: \"%s\"\n\n언급된 친구와의 관계가 어떠했는지, 그 관계가 사용자에게 어떤 의미였는지, 함께 겪었던 특정 사건에 대해 더 자세히 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserUpperElem = "단계: 초등 고학년(배움과 놀이)\n사용자 답변: \"%s\"\n\n언급된 활동(잘하는 것, 어려운 것, 취미)을 통해 사용자가 느꼈던 감정(성취감, 좌절감, 즐거움 등)과 그 경험을 통해 무엇을 배우게 되었는지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserUpperElemEvent = "단계: 초등 고학년(특별한 날)\n사용자 답변: \"%s\"\n\n묘사된 특별한 날(소풍, 학예회 등)의 경험이 왜 기억에 남았는지, 그날 느꼈던 가장 강렬한 감정은 무엇이었는지 묻는 후속 질문 2~3개를 작성하세요.";

    // --- 중학교 (4챕터) 프롬프트 ---
    private String dynUserMiddlePuberty = "단계: 중학교(사춘기)\n사용자 답변: \"%s\"\n\n언급된 신체적, 감정적 변화에 어떻게 적응해 나갔는지, 그로 인해 겪었던 혼란이나 남모를 고민이 있었는지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserMiddleMedia = "단계: 중학교(나의 세계)\n사용자 답변: \"%s\"\n\n언급된 미디어(음악, 영화 등)가 사용자의 어떤 감정을 대신해주었는지, 가장 위로가 되었던 가사나 장면이 있었는지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserMiddlePeople = "단계: 중학교(친구와 설렘)\n사용자 답변: \"%s\"\n\n언급된 친구 관계나 짝사랑 경험이 사용자의 학교생활에 어떤 영향을 주었는지, 그로 인해 변화된 생각이나 행동이 있었는지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserMiddleAcademics = "단계: 중학교(학업과 꿈)\n사용자 답변: \"%s\"\n\n언급된 학업 스트레스나 장래희망의 변화에 대해, 당시 어떤 고민을 했고, 누구와 그 고민을 나누었는지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserMiddleGrowth = "단계: 중학교(반항과 성장)\n사용자 답변: \"%s\"\n\n언급된 의문이나 반항이 어떤 결과를 가져왔는지, 그 경험을 통해 세상이나 자신에 대해 새롭게 깨닫게 된 점이 있었는지 묻는 후속 질문 2~3개를 작성하세요.";

    // --- 고등학교 (5챕터) 프롬프트 ---
    private String dynUserHighAcademics = "단계: 고등학교(학업과 진로)\n사용자 답변: \"%s\"\n\n언급된 목표나 어려움에 대해, 어떻게 극복하려 노력했는지, 그 과정에서 가장 큰 힘이 되어준 것은 무엇이었는지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserHighHobby = "단계: 고등학교(숨 쉴 공간)\n사용자 답변: \"%s\"\n\n언급된 '숨 쉴 공간'(취미, 음악 등)이 치열했던 시기를 견디는 데 구체적으로 어떤 도움이 되었는지, 가장 기억에 남는 순간은 언제였는지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserHighFriend = "단계: 고등학교(친구)\n사용자 답변: \"%s\"\n\n언급된 친구와 주로 어떤 이야기를 나누며 스트레스를 풀었는지, 그 우정이 당시 자신에게 어떤 의미였는지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserHighEvent = "단계: 고등학교(특별한 날)\n사용자 답변: \"%s\"\n\n묘사된 특별한 하루가 학업 스트레스 속에서 어떤 활력소가 되었는지, 그날의 경험이 남긴 것은 무엇인지 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserHighGrowth = "단계: 고등학교(성장)\n사용자 답변: \"%s\"\n\n언급된 사람이나 말이 사용자의 어떤 생각을 어떻게 바꾸었는지, 그 영향이 이후의 선택에 어떻게 작용했는지 묻는 후속 질문 2~3개를 작성하세요.";

    // --- 홀로서기 (6챕터) 프롬프트 ---
    private String dynUserTransition = "단계: 홀로서기(선택의 순간)\n사용자 답변: \"%s\"\n\n언급된 선택의 과정에서 가장 큰 고민은 무엇이었는지, 누구의 영향을 받았는지, 그리고 그 결정을 내렸을 때의 심정에 대해 더 깊이 묻는 후속 질문 2~3개를 작성하세요.";
    private String dynUserCollegeOrWork = "단계: 홀로서기(새로운 시작)\n사용자 답변: \"%s\"\n\n언급된 새로운 경험(대학, 직장 등) 속에서 가장 인상 깊었던 점은 무엇이었는지, 그 경험을 통해 무엇을 배우고 느꼈는지 묻는 후속 질문 2~3개를 작성하세요.";


    private String followupUserTemplate = "사용자가 이렇게 답했습니다:\n\"%s\"\n후속 질문을 한 문장으로 만들어 주세요.";
    private String nextQUserTemplate = "방금 제 답변은 이렇습니다: \"%s\". 이제 이어질 다음 인터뷰 질문을 한 문장으로 제안해주세요.";


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
