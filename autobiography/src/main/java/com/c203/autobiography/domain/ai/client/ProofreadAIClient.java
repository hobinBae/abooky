package com.c203.autobiography.domain.ai.client;

public interface ProofreadAIClient {
    String proofread(String originalText, Long bookCategory);
}
