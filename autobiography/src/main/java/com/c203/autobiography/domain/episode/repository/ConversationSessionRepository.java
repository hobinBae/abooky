package com.c203.autobiography.domain.episode.repository;

import com.c203.autobiography.domain.episode.entity.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, String> {
}
