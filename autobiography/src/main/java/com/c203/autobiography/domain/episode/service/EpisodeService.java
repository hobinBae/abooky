package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.episode.dto.EpisodeResponse;

public interface EpisodeService {

    EpisodeResponse createEpisode(Long memberId, Long bookId, String sessionId);

    EpisodeResponse getEpisode(Long episodeId);


}
