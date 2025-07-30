package com.c203.autobiography.domain.episode.conversation.service;

import com.c203.autobiography.domain.episode.conversation.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationMessageUpdateRequest;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationSessionRequest;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationSessionResponse;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationSessionUpdateRequest;
import com.c203.autobiography.domain.episode.conversation.dto.SessionStatus;
import com.c203.autobiography.domain.episode.conversation.entity.ConversationMessage;
import com.c203.autobiography.domain.episode.conversation.entity.ConversationSession;
import com.c203.autobiography.domain.episode.conversation.repository.ConversationMessageRepository;
import com.c203.autobiography.domain.episode.conversation.repository.ConversationSessionRepository;
import com.c203.autobiography.domain.template.service.QuestionTemplateService;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ConversationService 구현체: 세션과 메시지를 관리하며,
 * 질문 템플릿 인덱스를 자동으로 증가시킵니다.
 */
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationSessionRepository sessionRepo;
    private final ConversationMessageRepository messageRepo;
    private final QuestionTemplateService templateService;
    // 세션별 다음 질문 인덱스 관리 (0부터 시작)
    private final ConcurrentHashMap<String, AtomicInteger> indexMap = new ConcurrentHashMap<>();

    @Override
    public ConversationSessionResponse createSession(ConversationSessionRequest request) {
        ConversationSession session = ConversationSession.builder()
                .sessionId(request.getSessionId())
                .bookId(null)
                .episodeId(request.getEpisodeId())
                .templateIndex(0)
                .status(SessionStatus.OPEN)
                .tokenCount(0L)
                .build();
        sessionRepo.save(session);
        // 첫 질문은 템플릿 순서 0
        indexMap.put(request.getSessionId(), new AtomicInteger(0));
        return ConversationSessionResponse.from(session);
    }

    @Override
    public ConversationSessionResponse updateSession(ConversationSessionUpdateRequest request) {
        ConversationSession session = sessionRepo.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + request.getSessionId()));
        session = ConversationSession.builder()
                .sessionId(session.getSessionId())
                .bookId(session.getBookId())
                .episodeId(session.getEpisodeId())
                .templateIndex(request.getTemplateIndex())
                .status(request.getStatus())
                .tokenCount(session.getTokenCount())
                .build();
        sessionRepo.save(session);
        return ConversationSessionResponse.from(session);
    }

    @Override
    public ConversationSessionResponse getSession(String sessionId) {
        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));
        return ConversationSessionResponse.from(session);
    }

    @Override
    public String getNextQuestion(String sessionId) {
        AtomicInteger idx = indexMap.get(sessionId);
        if (idx == null) {
            throw new IllegalStateException("세션을 찾을 수 없습니다: " + sessionId);
        }
        // 1번 질문부터는 인덱스를 증가시켜서 가져옴
        int nextIndex = idx.incrementAndGet();
        return templateService.getByOrder(nextIndex);
    }

    @Override
    public ConversationMessageResponse createMessage(ConversationMessageRequest request) {
        int no = messageRepo.findBySessionIdOrderByMessageNo(request.getSessionId()).size();
        ConversationMessage msg = ConversationMessage.builder()
                .sessionId(request.getSessionId())
                .messageType(request.getMessageType())
                .chunkIndex(request.getChunkIndex())
                .content(request.getContent())
                .messageNo(no + 1)
                .build();
        messageRepo.save(msg);
        return ConversationMessageResponse.from(msg);
    }

    @Override
    public ConversationMessageResponse updateMessage(ConversationMessageUpdateRequest request) {
        ConversationMessage msg = messageRepo.findById(request.getMessageId())
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다: " + request.getMessageId()));
        msg = ConversationMessage.builder()
                .messageId(msg.getMessageId())
                .sessionId(msg.getSessionId())
                .messageType(msg.getMessageType())
                .chunkIndex(msg.getChunkIndex())
                .content(request.getContent())
                .messageNo(msg.getMessageNo())
                .build();
        messageRepo.save(msg);
        return ConversationMessageResponse.from(msg);
    }

    @Override
    public List<ConversationMessageResponse> getHistory(String sessionId) {
        return messageRepo.findBySessionIdOrderByMessageNo(sessionId).stream()
                .map(ConversationMessageResponse::from)
                .collect(Collectors.toList());
    }
}
