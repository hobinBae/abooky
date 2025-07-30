package com.c203.autobiography.domain.episode.conversation.service;

import com.c203.autobiography.domain.episode.conversation.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationMessageUpdateRequest;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationSessionRequest;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationSessionResponse;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationSessionUpdateRequest;
import java.util.List;

public interface ConversationService {
    ConversationSessionResponse createSession(ConversationSessionRequest request);
    ConversationSessionResponse updateSession(ConversationSessionUpdateRequest request);
    ConversationSessionResponse getSession(String sessionId);

    String getNextQuestion(String sessionId);

    ConversationMessageResponse createMessage(ConversationMessageRequest request);
    ConversationMessageResponse updateMessage(ConversationMessageUpdateRequest request);
    List<ConversationMessageResponse> getHistory(String sessionId);
}
