package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeUpdateRequest;

public interface EpisodeService {

    EpisodeResponse createEpisode(Long memberId, Long bookId, String sessionId);

    EpisodeResponse getEpisode(Long episodeId);

    EpisodeResponse updateEpisode(Long memberId, Long bookId, Long episodeId, EpisodeUpdateRequest request);

    Void deleteEpisode(Long memberId, Long bookId, Long episodeId);


}
