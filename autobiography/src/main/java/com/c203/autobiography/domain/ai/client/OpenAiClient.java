package com.c203.autobiography.domain.ai.client;

import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatMessage;
import com.c203.autobiography.domain.ai.service.OpenAiService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.service.OpenAPIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenAiClient implements AiClient{

    private final OpenAiService openAiService;
    private final OpenAiProperties props;

    @Override
    public String generateFollowUp(String lastAnswer){

        // 시스템 프롬프트: 후속 질문을 만들어 달라고 지시
        ChatMessage system = ChatMessage.of("system", props.getFollowupSystem());

        ChatMessage user = ChatMessage.of("user", String.format(props.getFollowupUserTemplate(), lastAnswer)
        );

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(props.getModel())
                .messages(List.of(system, user))
                .maxTokens(props.getMaxTokensFollowup())
                .temperature(props.getTemperature())
                .build();

        String followUp = openAiService.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent()
                .trim();

        return followUp.isBlank() ? null : followUp;

    }


}
