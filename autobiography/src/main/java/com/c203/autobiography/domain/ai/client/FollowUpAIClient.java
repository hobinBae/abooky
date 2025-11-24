package com.c203.autobiography.domain.ai.client;

public interface FollowUpAIClient {
    String generateDynamicFollowUpBySection(String sectionKey, String userAnswer, String nextTemplateQuestion);
}
