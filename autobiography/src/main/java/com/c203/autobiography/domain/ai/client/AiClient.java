package com.c203.autobiography.domain.ai.client;

import java.util.List;

public interface AiClient {



    String generateDynamicFollowUpBySection(String sectionKey, String userAnswer);
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
