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
    String generateNextQuestion(String sessionId, String userAnswer, List<String> history);
}
