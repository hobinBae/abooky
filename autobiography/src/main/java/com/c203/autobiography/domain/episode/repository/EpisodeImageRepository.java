package com.c203.autobiography.domain.episode.repository;

import com.c203.autobiography.domain.episode.entity.EpisodeImage;
import com.c203.autobiography.domain.episode.entity.EpisodeImageId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeImageRepository extends JpaRepository<EpisodeImage, EpisodeImageId> {
}
