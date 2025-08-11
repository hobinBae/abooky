package com.c203.autobiography.domain.groupbook.episode.repository;

import com.c203.autobiography.domain.groupbook.episode.entity.EpisodeGuideState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpisodeGuideStateRepository extends JpaRepository<EpisodeGuideState, Long> {
    List<EpisodeGuideState> findByEpisode_IdOrderByStepNoAsc(Long episodeId);
}
