package com.c203.autobiography.domain.groupbook.episode.repository;

import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupEpisodeRepository extends JpaRepository<GroupEpisode, Long> {
    List<GroupEpisode> findByGroupBook_GroupBookIdOrderByOrderNoAscCreatedAtAsc(Long groupBookId);
}
