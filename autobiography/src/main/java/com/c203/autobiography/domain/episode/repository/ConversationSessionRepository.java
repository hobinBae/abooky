package com.c203.autobiography.domain.episode.repository;

import com.c203.autobiography.domain.episode.entity.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, String> {
    @Modifying
    @Query("""
   UPDATE ConversationSession s
      SET s.episodeId = :episodeId
    WHERE s.sessionId = :sessionId
""")
    void updateEpisodeId(String sessionId, Long episodeId);
}
