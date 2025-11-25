package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.episode.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.dto.MessageType;
import com.c203.autobiography.domain.episode.entity.ConversationMessage;
import com.c203.autobiography.domain.episode.entity.ConversationSession;
import com.c203.autobiography.domain.episode.repository.ConversationMessageRepository;
import com.c203.autobiography.domain.episode.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationMessageServiceImpl implements ConversationMessageService{

    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    @Override
    public void deleteLastQuestion(String sessionId) {

    }

    @Override
    @Transactional
    public ConversationMessageResponse createMessage(ConversationMessageRequest request) {
        int lastNo = conversationMessageRepository.findTopBySessionIdOrderByMessageNoDesc(request.getSessionId())
                .map(ConversationMessage::getMessageNo)
                .orElse(0);

        ConversationMessage msg = ConversationMessage.builder()
                .sessionId(request.getSessionId())
                .messageType(request.getMessageType())
                .chunkIndex(request.getChunkIndex())
                .content(request.getContent())
                .messageNo(lastNo + 1)
                .build();
        conversationMessageRepository.save(msg);

        if (request.getMessageType() == MessageType.ANSWER) {
            long add = estimateTokens(request.getContent());
            conversationSessionRepository.findById(request.getSessionId()).ifPresent(s -> {
                ConversationSession updated = s.toBuilder()
                        .tokenCount((s.getTokenCount() == null ? 0L : s.getTokenCount()) + add)
                        .build();
                conversationSessionRepository.save(updated);
            });
        }
        return ConversationMessageResponse.from(msg);
    }

    private long estimateTokens(String text) {
        if (text == null) {
            return 0;
        }
// 한글 기준 대략 2~3자당 1토큰으로 가정
        return Math.max(1, text.length() / 3);
    }

}
