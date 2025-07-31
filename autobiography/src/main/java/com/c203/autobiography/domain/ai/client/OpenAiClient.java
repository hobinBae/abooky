package com.c203.autobiography.domain.ai.client;

import jakarta.validation.Valid;
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

//    @Value("${openai.model}")
    private String model = "gpt-4o-mini";

    @Override
    public String generateFollowUp(String lastAnswer){
        // 시스템 프롬프트: 후속 질문을 만들어 달라고 지시
        Cha
    }

//    @Override
//    public String generateNextQuestion(String sessionId, String userAnswer, List<String> history) {
//        // 1) 이전 대화 내역을 system/user/assistant 역할로 포맷팅
//        List<ChatMessage> messages = history.stream()
//                .map(msg -> {
//                    // 간단히 "USER:"로 모두 표시 — 필요시 구분자를 더 정교하게
//                    return new ChatMessage("user", msg);
//                })
//                .collect(Collectors.toList());
//
//        // 2) 마지막에 지금 답변을 user로 추가
//        messages.add(new ChatMessage("user", "방금 제 답변은 이렇습니다: \"" + userAnswer + "\". "\
//                + "이제 이 답변 기반으로 이어질 다음 인터뷰 질문을 한 문장으로 제안해주세요."));
//
//        ChatCompletionRequest request = ChatCompletionRequest.builder()
//                .model(model)
//                .messages(messages)
//                .temperature(0.7)
//                .maxTokens(100)
//                .build();
//
//        ChatCompletionResult result = openAiService.createChatCompletion(request);
//        return result.getChoices().get(0).getMessage().getContent().trim();
//    }
}
