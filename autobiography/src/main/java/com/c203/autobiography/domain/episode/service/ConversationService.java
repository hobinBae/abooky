package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.episode.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.dto.ConversationMessageUpdateRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionResponse;
import com.c203.autobiography.domain.episode.dto.ConversationSessionUpdateRequest;
import com.c203.autobiography.domain.episode.dto.StartConversationResponse;
import com.c203.autobiography.domain.episode.entity.ConversationSession;

import com.c203.autobiography.domain.episode.template.dto.NextQuestionDto;
import java.util.List;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ConversationService {

    // == 세션/메시지 기본 관리 기능 ==
    ConversationSessionResponse createSession(ConversationSessionRequest request);
    ConversationSessionResponse updateSession(ConversationSessionUpdateRequest request);
    ConversationSessionResponse getSession(String sessionId);
    ConversationSession getSessionEntity(String sessionId);
    ConversationMessageResponse createMessage(ConversationMessageRequest request);
    ConversationMessageResponse updateMessage(ConversationMessageUpdateRequest request);
    List<ConversationMessageResponse> getHistory(String sessionId);
    String getLastAnswer(String sessionId);
    String getLastQuestion(String sessionId);

    // == 대화 흐름(질문) 관리 기능 (기존 ChapterBasedQuestionService 역할) ==
    /**
     * 챕터 기반 대화 세션을 초기화하고 첫 번째 질문을 반환합니다.
     */
    NextQuestionDto initializeSession(String sessionId, Long bookId);

    /**
     * 사용자 답변을 기반으로 다음 질문을 생성하고 반환합니다.
     */
    NextQuestionDto getNextQuestion(Long memberId, Long bookId, Long episodeId, String sessionId, String userAnswer);

    String startNewConversation(Long memberId, Long bookId, Long episodeId);

    // 기존 대화 재연결
    void establishConversationStream(String sessionId, Long bookId, SseEmitter emitter);


}