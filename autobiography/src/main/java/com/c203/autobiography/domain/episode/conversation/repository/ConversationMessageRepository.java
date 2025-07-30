package com.c203.autobiography.domain.episode.conversation.repository;

import com.c203.autobiography.domain.episode.conversation.entity.ConversationMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
    List<ConversationMessage> findBySessionIdOrderByMessageNo(String sessionId);
}
