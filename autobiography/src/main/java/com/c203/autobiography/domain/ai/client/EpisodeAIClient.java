package com.c203.autobiography.domain.ai.client;

public interface EpisodeAIClient {
    String generateEpisode(String chapterId, String dialog, boolean jsonMode);
}
