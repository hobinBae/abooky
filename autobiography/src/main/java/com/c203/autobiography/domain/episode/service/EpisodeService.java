package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeUpdateRequest;
import com.c203.autobiography.domain.episode.entity.Episode;
import com.c203.autobiography.domain.member.entity.Member;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface EpisodeService {

    EpisodeResponse createEpisode(Long memberId, Long bookId);

    EpisodeResponse getEpisode(Long episodeId);

    EpisodeResponse updateEpisode(Long memberId, Long bookId, Long episodeId, EpisodeUpdateRequest request);

    Void deleteEpisode(Long memberId, Long bookId, Long episodeId);

    EpisodeResponse createEpisodeFromCurrentWindow(Episode episode, String sessionId) throws JsonProcessingException;

}
