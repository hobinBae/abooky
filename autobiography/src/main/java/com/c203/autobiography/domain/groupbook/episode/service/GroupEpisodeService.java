package com.c203.autobiography.domain.groupbook.episode.service;

import com.c203.autobiography.domain.groupbook.episode.dto.EpisodeCreateRequest;
import com.c203.autobiography.domain.groupbook.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.groupbook.episode.dto.StepNextRequest;
import com.c203.autobiography.domain.groupbook.episode.dto.StepNextResponse;

public interface GroupEpisodeService {
    EpisodeResponse create(Long groupId, Long groupBookId, EpisodeCreateRequest request, Long memberId);
    StepNextResponse next(Long groupId, Long groupBookId, Long episodeId, StepNextRequest req, Long memberId);
    EpisodeResponse get(Long groupId, Long groupBookId, Long episodeId);
    EpisodeResponse finalizeEpisode(Long groupId, Long groupBookId, Long episodeId);

}
