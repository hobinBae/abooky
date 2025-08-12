package com.c203.autobiography.domain.groupbook.episode.service;

import com.c203.autobiography.domain.groupbook.episode.dto.*;

import java.util.List;

public interface GroupEpisodeService {
    GroupEpisodeResponse create(Long groupId, Long groupBookId, GroupEpisodeCreateRequest request, Long memberId);

    StepNextResponse next(Long groupId, Long groupBookId, Long episodeId, StepNextRequest req, Long memberId);

    GroupEpisodeResponse get(Long groupId, Long groupBookId, Long episodeId);

    GroupEpisodeResponse finalizeEpisode(Long groupId, Long groupBookId, Long episodeId);

    List<GroupEpisodeResponse> getEpisodeList(Long groupId, Long groupBookId, Long memberId);

    GroupEpisodeResponse update(Long groupId, Long groupBookId, Long episodeId, GroupEpisodeUpdateRequest request, Long memberId);

    void delete(Long groupId, Long groupBookId, Long episodeId, Long memberId);
}
