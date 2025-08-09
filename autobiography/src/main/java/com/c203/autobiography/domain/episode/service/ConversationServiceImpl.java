package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.ai.client.AiClient;
import com.c203.autobiography.domain.episode.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.template.service.ChapterBasedQuestionService;
import com.c203.autobiography.domain.episode.template.dto.NextQuestionDto;
import com.c203.autobiography.domain.episode.dto.ConversationMessageUpdateRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionResponse;
import com.c203.autobiography.domain.episode.dto.ConversationSessionUpdateRequest;
import com.c203.autobiography.domain.episode.dto.MessageType;
import com.c203.autobiography.domain.episode.dto.SessionStatus;
import com.c203.autobiography.domain.episode.entity.ConversationMessage;
import com.c203.autobiography.domain.episode.entity.ConversationSession;
import com.c203.autobiography.domain.episode.repository.ConversationMessageRepository;
import com.c203.autobiography.domain.episode.repository.ConversationSessionRepository;
import com.c203.autobiography.domain.episode.template.service.QuestionTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private final ChapterBasedQuestionService chapterBasedService;
    // 세션별 질문 큐 관리 (0부터 시작)
    private final ConcurrentHashMap<String, Deque<String>> questionQueueMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> lastQuestionMap = new ConcurrentHashMap<>();

    @Override
    public ConversationSessionResponse createSession(ConversationSessionRequest request) {
        ConversationSession session = ConversationSession.builder()
                .sessionId(request.getSessionId())
                .bookId(null)
                .episodeId(request.getEpisodeId())
                .status(SessionStatus.OPEN)
                .tokenCount(0L)
                .templateIndex(0)
                .build();
        sessionRepo.save(session);
        // 템플릿 전체를 순서대로 큐에 담기
        List<String> allTemplates = templateService.getAllInorder();
        Deque<String> queue = new ArrayDeque<>(allTemplates);
        questionQueueMap.put(request.getSessionId(), queue);
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
                .templateIndex(request.getTemplateIndex())
                .build();
        sessionRepo.save(session);
        // 큐 재초기화
        List<String> allTemplates = templateService.getAllInorder();
        questionQueueMap.put(request.getSessionId(), new ArrayDeque<>(allTemplates));
        return ConversationSessionResponse.from(session);
    }

    @Override
    public ConversationSessionResponse getSession(String sessionId) {
        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));
        return ConversationSessionResponse.from(session);
    }
    
    @Override
    public ConversationSession getSessionEntity(String sessionId) {
        return sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));
    }

    @Override
    public String getNextQuestion(String sessionId) {
        // 새로운 챕터 기반 서비스 사용 여부 확인
        ConversationSession session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null && session.getCurrentChapterId() != null) {
            // 챕터 기반 모드
            try {
                String lastAnswer = getLastAnswer(sessionId);
                NextQuestionDto nextQuestionDto = chapterBasedService.getNextQuestion(sessionId, lastAnswer);
                String next = nextQuestionDto.getQuestionText();
                
                if (next != null) {
                    lastQuestionMap.put(sessionId, next);
                }
                return next;
            } catch (Exception e) {
                // 챕터 기반 서비스에서 오류 발생시 레거시 모드로 fallback
                // log.warn("챕터 기반 서비스 오류, 레거시 모드로 전환: {}", e.getMessage());
            }
        }
        
        // 레거시 모드 (기존 로직)
        Deque<String> queue = questionQueueMap.get(sessionId);
        if (queue == null || queue.isEmpty()) {
            return null;
        }

        String lastAnswer = getLastAnswer(sessionId);
        String next;
        if (lastAnswer.isBlank()) {
            next = queue.poll();
        } else {
            String followUp = aiClient.generateFollowUp(lastAnswer);
            if (shouldInjectFollowUp(lastAnswer, followUp)) {
                queue.addFirst(followUp);
            }
            next = queue.poll();
        }

        if (next != null) {
            // (2) "마지막 질문" 맵에 저장
            lastQuestionMap.put(sessionId, next);
        }
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

    @Override
    public String getLastAnswer(String sessionId) {
        return messageRepo
                .findFirstBySessionIdAndMessageTypeOrderByMessageNoDesc(sessionId, MessageType.FINAL)
                .map(ConversationMessage::getContent)
                .orElse("");
    }

    private boolean shouldInjectFollowUp(String lastAnswer, String followUp) {
        if (followUp == null || followUp.isBlank()) return false;
        return !followUp.trim().equalsIgnoreCase(lastAnswer.trim());
    }
    @Override
    public String getLastQuestion(String sessionId) {
        return lastQuestionMap.getOrDefault(sessionId, "");
    }

}
