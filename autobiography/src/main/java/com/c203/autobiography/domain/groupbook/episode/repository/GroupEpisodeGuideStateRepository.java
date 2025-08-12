package com.c203.autobiography.domain.groupbook.episode.repository;

import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisodeGuideState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupEpisodeGuideStateRepository extends JpaRepository<GroupEpisodeGuideState, Long> {
    List<GroupEpisodeGuideState> findByGroupEpisode_GroupEpisodeIdOrderByStepNoAsc(Long episodeId);

    // 추가 필요한 메서드들 (같은 패턴으로)
    Optional<GroupEpisodeGuideState> findByGroupEpisode_GroupEpisodeIdAndStepNo(Long episodeId, Integer stepNo);

    List<GroupEpisodeGuideState> findByGroupEpisode_GroupEpisodeIdAndIsFinalTrue(Long episodeId);

}
