package com.c203.autobiography.domain.episode.repository;

import com.c203.autobiography.domain.episode.dto.SessionStatus;
import com.c203.autobiography.domain.episode.entity.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, String> {
    @Modifying
    @Query("""
   UPDATE ConversationSession s
      SET s.episodeId = :episodeId
    WHERE s.sessionId = :sessionId
""")
    void updateEpisodeId(String sessionId, Long episodeId);
    // 특정 에피소드에 대해 OPEN 상태인 가장 최신 세션을 찾는 메소드
    Optional<ConversationSession> findTopByEpisodeIdAndStatusOrderByCreatedAtDesc(Long episodeId, SessionStatus status);
}
