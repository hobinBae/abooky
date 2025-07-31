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

    /**
     * 사용자의 마지막 답변을 받아서
     * 자연스럽고 문맥에 맞는 후속 질문을 생성하여 반환합니다.
     */
    String generateFollowUp(String lastAnswer);
}
