package com.c203.autobiography.domain.ai.client;

import java.util.List;

public interface AiClient {
    /**
     * 사용자 답변과 문맥을 받아 다음 질문을 생성합니다.
     * @param sessionId
     * @param userAnswer
     * @param history
     * @return
     */
//    String generateNextQuestion(String sessionId, String userAnswer, List<String> history);

    /**
     * 사용자의 마지막 답변을 받아서
     * 자연스럽고 문맥에 맞는 후속 질문을 생성하여 반환합니다.
     */
    String generateFollowUp(String lastAnswer);
    
    /**
     * 챕터 기반 동적 후속질문을 생성합니다.
     * 주어진 프롬프트 템플릿과 사용자 답변을 기반으로 2-3개의 후속질문을 생성합니다.
     */
    String generateDynamicFollowUp(String promptTemplate, String userAnswer);
    
    /**
     * 대화 내용을 바탕으로 자서전 에피소드를 생성합니다.
     */
    String generateEpisode(String conversationHistory);
    
    /**
     * 사용자 답변을 분석하여 이미 답변된 질문들을 식별합니다.
     * @param userAnswer 사용자의 답변
     * @param remainingQuestions 남은 후속질문 리스트
     * @return 아직 답변되지 않은 질문들의 인덱스 리스트
     */
    String analyzeAnsweredQuestions(String userAnswer, String remainingQuestions);
}
