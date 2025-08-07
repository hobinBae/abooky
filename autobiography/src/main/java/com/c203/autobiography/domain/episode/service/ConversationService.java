package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.episode.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.dto.ConversationMessageUpdateRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionResponse;
import com.c203.autobiography.domain.episode.dto.ConversationSessionUpdateRequest;
import com.c203.autobiography.domain.episode.entity.ConversationSession;

import java.util.List;

public interface ConversationService {
    ConversationSessionResponse createSession(ConversationSessionRequest request);
    ConversationSessionResponse updateSession(ConversationSessionUpdateRequest request);
    ConversationSessionResponse getSession(String sessionId);
    
    ConversationSession getSessionEntity(String sessionId);
    String getNextQuestion(String sessionId);
    
    String getLastAnswer(String sessionId);

    ConversationMessageResponse createMessage(ConversationMessageRequest request);
    ConversationMessageResponse updateMessage(ConversationMessageUpdateRequest request);
    List<ConversationMessageResponse> getHistory(String sessionId);
    public String getLastQuestion(String sessionId);
}
