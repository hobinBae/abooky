package com.c203.autobiography.domain.ai.service;

;
import com.c203.autobiography.domain.ai.client.OpenAiProperties;
import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatCompletionResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiServiceImpl implements OpenAiService {

    private final WebClient webClient;         // Spring Boot 에서 커넥터 풀, 타임아웃 자동 관리
    private final OpenAiProperties props;

    @Override
    public ChatCompletionResponse createChatCompletion(ChatCompletionRequest request) {
        log.info(props.getApiUrl());
        log.info(props.getApiKey());
        return webClient.post()
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        Mono.error(new RuntimeException("OpenAI error: " + resp.statusCode()))
                )
                .bodyToMono(ChatCompletionResponse.class)
                .block(Duration.ofSeconds(props.getRequestTimeoutSec()));
    }


}