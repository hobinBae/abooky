package com.c203.autobiography.domain.episode.repository;

import com.c203.autobiography.domain.episode.dto.MessageType;
import com.c203.autobiography.domain.episode.entity.ConversationMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
    List<ConversationMessage> findBySessionIdOrderByMessageNo(String sessionId);

    Optional<ConversationMessage> findTopBySessionIdOrderByMessageNoDesc(String sessionId);

    Optional<ConversationMessage> findFirstBySessionIdAndMessageTypeOrderByMessageNoDesc(String sessionId,
                                                                                         MessageType type);

    List<ConversationMessage> findBySessionIdAndMessageNoBetweenOrderByMessageNo(
            String sessionId, Integer startInclusive, Integer endInclusive);

    @Query(
            "select max(m.messageNo) from ConversationMessage m where m.sessionId = :sessionId"
    )
    Integer findMaxMessageNo(String sessionId);

    @Query(
            "select max(m.messageNo) from ConversationMessage m where m.sessionId = :sessionId"
    )
    Optional<Integer> findTopMessageNoBySessionId(String sessionId); // 네이티브 or JPQL
}
