package com.c203.autobiography.domain.ai.client;

import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.properties.OpenAiProperties;
import com.c203.autobiography.domain.ai.service.OpenAiService;

public abstract class BaseAIClient {
    protected final OpenAiService openAiService;
    protected final OpenAiProperties props;

    protected BaseAIClient(OpenAiService openAiService, OpenAiProperties props) {
        this.openAiService = openAiService;
        this.props = props;
    }

    protected String call(ChatCompletionRequest request) {
        return openAiService.createChatCompletion(request)
                .getChoices().get(0)
                .getMessage().getContent()
                .trim();
    }
}
