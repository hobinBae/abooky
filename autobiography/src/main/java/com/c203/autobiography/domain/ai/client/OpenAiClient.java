package com.c203.autobiography.domain.ai.client;

import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatMessage;
import com.c203.autobiography.domain.ai.service.OpenAiService;
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
        ChatMessage system = ChatMessage.of("system",
                "당신은 자서전을 작성하는 친절한 인터뷰어입니다. " +
                        "사용자의 답변을 듣고, 추가 질문을 통해 더 깊이 있는 에피소드를 이끌어낼 수 있다고 판단되면, " +
                        "자연스럽고 문맥에 맞는 한 문장의 후속 질문을 생성해주세요. " +
                        "만약 없다면 질문을 하지 않아도 괜찮습니다.");

        ChatMessage user = ChatMessage.of("user",
                "사용자가 이렇게 답했습니다:\n\"" + lastAnswer + "\"\n" +
                        "이 답변에 대한 후속 질문을 한 문장으로 만들어 주세요."
        );
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(system, user))
                .maxTokens(50)
                .temperature(0.7)
                .build();

        String followUp = openAiService.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent()
                .trim();

        return followUp;

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
