package com.c203.autobiography.domain.ai.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiClientFactory {
    private final FollowUpAIClient followUpAIClient;
    private final EpisodeAIClient episodeAIClient;
    private final ProofreadAIClient proofreadAIClient;
    private final AnswerAnalysisAIClient answerAnalysisAIClient;
    private final EditAIClient editAIClient;

    public FollowUpAIClient followUp() {
        return followUpAIClient;
    }

    public EpisodeAIClient episode() {
        return episodeAIClient;
    }

    public ProofreadAIClient proofread() {
        return proofreadAIClient;
    }

    public AnswerAnalysisAIClient analysis() {
        return answerAnalysisAIClient;
    }

    public EditAIClient edit() {
        return editAIClient;
    }
}
