package com.c203.autobiography.domain.episode.conversation.service;

import com.c203.autobiography.domain.ai.client.AiClient;
import com.c203.autobiography.domain.episode.conversation.dto.*;
import com.c203.autobiography.domain.episode.conversation.entity.ConversationMessage;
import com.c203.autobiography.domain.episode.conversation.entity.ConversationSession;
import com.c203.autobiography.domain.episode.conversation.repository.ConversationMessageRepository;
import com.c203.autobiography.domain.episode.conversation.repository.ConversationSessionRepository;
import com.c203.autobiography.domain.template.service.QuestionTemplateService;
import java.util.ArrayDeque;
import java.util.Deque;
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
    private final AiClient aiClient;
    // 세션별 다음 질문 인덱스 관리 (0부터 시작)
//    private final ConcurrentHashMap<String, AtomicInteger> indexMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Deque<String>> questionQueueMap = new ConcurrentHashMap<>();
    @Override
    public ConversationSessionResponse createSession(ConversationSessionRequest request) {
        ConversationSession session = ConversationSession.builder()
                .sessionId(request.getSessionId())
                .bookId(null)
                .episodeId(request.getEpisodeId())
                .status(SessionStatus.OPEN)
                .tokenCount(0L)
                .build();
        sessionRepo.save(session);
        // 템플릿 전체를 순서대로 가져와 큐에 담기
        List<String> allTemplates = templateService.getAllInorder();
        Deque<String> queue = new ArrayDeque<>(allTemplates);
        questionQueueMap.put(session.getSessionId(), queue);
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
                .status(request.getStatus())
                .tokenCount(session.getTokenCount())
                .build();
        sessionRepo.save(session);
        List<String> allTemplates = templateService.getAllInorder();
        Deque<String> queue = new ArrayDeque<>(allTemplates);
        questionQueueMap.put(session.getSessionId(), queue);
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

        Deque<String> queue = questionQueueMap.get(sessionId);
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        String lastAnswer = getLastAnswer(sessionId);

        String followUp = aiClient.generateFollowUp(lastAnswer);

        if (shouldInjectFollowUp(lastAnswer, followUp)) {
            queue.addFirst(followUp);
        }
        String next = queue.poll();
        if (next == null) {
            // 더 이상 질문이 없으면 null 혹은 기본 행동
            return null;
        }

        createMessage(ConversationMessageRequest.builder()
                .sessionId(sessionId)
                .messageType(MessageType.QUESTION)
                .content(next)
                .build());
        return next;

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

    /**
     * 세션의 마지막 FINAL(사용자 최종 답변) 메시지 내용을 가져옵니다.
     */
    private String getLastAnswer(String sessionId) {
        return messageRepo
                .findFirstBySessionIdAndMessageTypeOrderByMessageNoDesc(sessionId, MessageType.FINAL)
                .map(ConversationMessage::getContent)
                .orElse("");
    }

    /**
     * AI가 생성한 후속 질문을 주입할지 결정합니다.
     * (예: 후속 질문이 널이 아니고, 기존 질문/답변과 중복이 아닐 때만)
     */
    private boolean shouldInjectFollowUp(String lastAnswer, String followUp) {
        if (followUp == null || followUp.isBlank()) return false;
        // 답변과 동일한 질문을 주입할 필요는 없으므로 필터링
        return !followUp.trim().equalsIgnoreCase(lastAnswer.trim());
    }

}
