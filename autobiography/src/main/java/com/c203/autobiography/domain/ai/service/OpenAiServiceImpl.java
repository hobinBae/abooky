package com.c203.autobiography.domain.ai.service;

;
import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatCompletionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OpenAiServiceImpl implements OpenAiService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions";

    @Override
    public ChatCompletionResponse createChatCompletion(ChatCompletionRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ChatCompletionRequest> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<ChatCompletionResponse> resp =
                restTemplate.exchange(apiUrl, HttpMethod.POST, entity,
                        ChatCompletionResponse.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("OpenAI API 호출 실패: " + resp.getStatusCode());
        }
        return resp.getBody();
    }
}