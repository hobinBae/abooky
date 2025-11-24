package com.c203.autobiography.domain.ai.client;

public interface AnswerAnalysisAIClient {
    String analyzeAnsweredQuestions(String userAnswer, String remainingQuestions);
}
