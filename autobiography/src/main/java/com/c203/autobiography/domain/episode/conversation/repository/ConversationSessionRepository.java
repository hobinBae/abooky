package com.c203.autobiography.domain.episode.conversation.repository;

import com.c203.autobiography.domain.episode.conversation.entity.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, String> {
}
