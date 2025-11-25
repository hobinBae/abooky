package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.episode.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.dto.ConversationMessageResponse;

public interface ConversationMessageService {
    void deleteLastQuestion(String sessionId);

    ConversationMessageResponse createMessage(ConversationMessageRequest request);
}
