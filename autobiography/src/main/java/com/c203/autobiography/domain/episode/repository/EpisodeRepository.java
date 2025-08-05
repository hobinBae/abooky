package com.c203.autobiography.domain.episode.repository;

import com.c203.autobiography.domain.episode.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    Optional<Episode> findByEpisodeIdAndDeletedAtIsNull(Long episodeId);
}
