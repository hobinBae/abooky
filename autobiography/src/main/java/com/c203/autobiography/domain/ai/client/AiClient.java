package com.c203.autobiography.domain.ai.client;

import java.util.List;

public interface AiClient {



    String generateDynamicFollowUpBySection(String sectionKey, String userAnswer, String nextTemplateQuestion);
//    /**
//     * 대화 내용을 바탕으로 자서전 에피소드를 생성합니다.
//     */
//    String generateEpisode(String conversationHistory, boolean jsonMode);
//
    /**
     * [수정] chapterId 파라미터 추가
     * @param chapterId 생성할 에피소드에 해당하는 챕터 ID (예: "chapter1", "chapter2")
     * @param dialog    에피소드 생성을 위한 원본 대화 내용
     * @param jsonMode  true일 경우, OpenAI의 JSON 모드를 활성화
     * @return AI가 생성한 원본 응답 문자열 (JSON 형식)
     */
    String generateEpisode(String chapterId, String dialog, boolean jsonMode);

    /**
     * 사용자 답변을 분석하여 이미 답변된 질문들을 식별합니다.
     * @param userAnswer 사용자의 답변
     * @param remainingQuestions 남은 후속질문 리스트
     * @return 아직 답변되지 않은 질문들의 인덱스 리스트
     */
    String analyzeAnsweredQuestions(String userAnswer, String remainingQuestions);

    /**
     * 질문과 답변을 바탕으로 답변을 교정합니다.
     * @param question 원본 질문
     * @param answer 사용자의 답변
     * @param template 현재 템플릿 (INTRO, STORY, REFLECTION, FUTURE)
     * @param groupType 그룹 타입 (FAMILY, FRIENDS, COUPLE, TEAM, OTHER)
     * @param correctionStyle 교정 스타일 (FORMAL, CASUAL, LITERARY, CONCISE)
     * @return 교정된 답변과 개선 사항을 포함한 JSON 응답
     */
    String correctAnswerWithContext(String question, String answer, String template, String groupType, String correctionStyle);

    /**
     * 사용자 답변을 읽기 쉬운 문장으로 편집합니다.(그룹책 에피소드 생성용)
     * @param rawAnswer
     * @param priorContext
     * @param tone
     * @return
     */
    String editText(String rawAnswer, String priorContext, String tone);

    String proofread(String originalText, Long bookCategory);
}
