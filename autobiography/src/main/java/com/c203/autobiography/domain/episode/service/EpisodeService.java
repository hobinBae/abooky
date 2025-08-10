package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeUpdateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface EpisodeService {

    EpisodeResponse createEpisode(Long bookId, String sessionId) throws JsonProcessingException;

    EpisodeResponse getEpisode(Long episodeId);

    EpisodeResponse updateEpisode(Long memberId, Long bookId, Long episodeId, EpisodeUpdateRequest request);

    Void deleteEpisode(Long memberId, Long bookId, Long episodeId);

    EpisodeResponse createEpisodeFromCurrentWindow(Long bookId, String sessionId) throws JsonProcessingException;

}
