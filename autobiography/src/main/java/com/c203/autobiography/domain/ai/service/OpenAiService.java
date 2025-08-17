package com.c203.autobiography.domain.ai.service;

import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatCompletionResponse;

public interface OpenAiService {

    /**
     * Chat Completion 호출
     *
     */
    ChatCompletionResponse createChatCompletion(ChatCompletionRequest request);


}
