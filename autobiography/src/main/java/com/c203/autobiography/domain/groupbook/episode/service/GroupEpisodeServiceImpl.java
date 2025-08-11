package com.c203.autobiography.domain.groupbook.episode.service;

import com.c203.autobiography.domain.groupbook.episode.dto.EpisodeCreateRequest;
import com.c203.autobiography.domain.groupbook.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.groupbook.episode.dto.StepNextRequest;
import com.c203.autobiography.domain.groupbook.episode.dto.StepNextResponse;
import com.c203.autobiography.domain.groupbook.episode.repository.EpisodeGuideStateRepository;
import com.c203.autobiography.domain.groupbook.episode.repository.GroupEpisodeRepository;
import com.c203.autobiography.domain.groupbook.repository.GroupBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupEpisodeServiceImpl implements GroupEpisodeService {

    private final GroupBookRepository groupBookRepository;
    private final GroupEpisodeRepository groupEpisodeRepository;
    private final EpisodeGuideStateRepository stateRepository;
    private final GuideResolver guideResolver;
    private final EditorService editorService;


    @Override
    @Transactional
    public EpisodeResponse create(Long groupId, Long groupBookId, EpisodeCreateRequest request, Long memberId) {
        return null;
    }

    @Override
    @Transactional
    public StepNextResponse next(Long groupId, Long groupBookId, Long episodeId, StepNextRequest req, Long memberId) {
        return null;
    }

    @Override
    public EpisodeResponse get(Long groupId, Long groupBookId, Long episodeId) {
        return null;
    }

    @Override
    @Transactional
    public EpisodeResponse finalizeEpisode(Long groupId, Long groupBookId, Long episodeId) {
        return null;
    }
}
