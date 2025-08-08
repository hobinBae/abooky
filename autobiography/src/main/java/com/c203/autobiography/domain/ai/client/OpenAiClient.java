package com.c203.autobiography.domain.ai.client;

import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatMessage;
import com.c203.autobiography.domain.ai.service.OpenAiService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);
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
    
    @Override
    public String generateDynamicFollowUp(String promptTemplate, String userAnswer) {
        ChatMessage system = ChatMessage.of("system", props.getDynamicFollowUpSystem());
        ChatMessage user = ChatMessage.of("user", String.format(promptTemplate, userAnswer));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(props.getModel())
                .messages(List.of(system, user))
                .maxTokens(props.getMaxTokensNext())
                .temperature(props.getTemperature())
                .build();

        String response = openAiService.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent()
                .trim();

        return response.isBlank() ? null : response;
    }

    @Override
    public String generateDynamicFollowUpBySection(String sectionKey, String userAnswer) {
        String userPrompt;
        switch (sectionKey) {
            case "INTRO" -> userPrompt = String.format(props.getDynUserIntro(), userAnswer);
            case "CHILDHOOD" -> userPrompt = String.format(props.getDynUserChildhood(), userAnswer);
            case "UPPER_ELEM" -> userPrompt = String.format(props.getDynUserUpperElem(), userAnswer);
            case "MIDDLE" -> userPrompt = String.format(props.getDynUserMiddle(), userAnswer);
            case "HIGH" -> userPrompt = String.format(props.getDynUserHigh(), userAnswer);
            case "TRANSITION" -> userPrompt = String.format(props.getDynUserTransition(), userAnswer);
            case "COLLEGE_OR_WORK" -> userPrompt = String.format(props.getDynUserCollegeOrWork(), userAnswer);
            default -> userPrompt = String.format(props.getDynUserIntro(), userAnswer);
        }
        ChatMessage system = ChatMessage.of("system", props.getDynamicFollowUpSystem());
        ChatMessage user = ChatMessage.of("user", userPrompt);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(props.getModel())
                .messages(List.of(system, user))
                .maxTokens(props.getMaxTokensNext())
                .temperature(props.getTemperature())
                .build();

        String response = openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent().trim();
        return response.isBlank() ? null : response;
    }
    
    @Override
    public String generateEpisode(String conversationHistory) {
        ChatMessage system = ChatMessage.of("system", props.getEpisodeGenerationSystem());
        ChatMessage user = ChatMessage.of("user", String.format(props.getEpisodeGenerationTemplate(), conversationHistory));
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(props.getModel())
                .messages(List.of(system, user))
                .maxTokens(500) // 에피소드는 더 긴 텍스트 필요
                .temperature(0.8) // 창의적인 글쓰기를 위해 temperature 높임
                .build();
        
        String episode = openAiService.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent()
                .trim();
        
        return episode.isBlank() ? null : episode;
    }
    
    @Override
    public String analyzeAnsweredQuestions(String userAnswer, String remainingQuestions) {
        ChatMessage system = ChatMessage.of("system", props.getAnswerAnalysisSystem());
        ChatMessage user = ChatMessage.of("user", String.format(props.getAnswerAnalysisTemplate(), userAnswer, remainingQuestions));
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(props.getModel())
                .messages(List.of(system, user))
                .maxTokens(50) // 짧은 응답만 필요
                .temperature(0.1) // 정확성 우선
                .build();
        
        String analysis = openAiService.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent()
                .trim();
        
        return analysis.isBlank() ? null : analysis;
    }

    // 추가: JSON 강제 포맷 에피소드
    public String generateEpisodeJson(String instructionAndDialog) {
        ChatMessage system = ChatMessage.of("system", "너는 JSON 포맷을 정확히 지키는 도우미이다. 키는 title, content만 사용.");
        ChatMessage user = ChatMessage.of("user", instructionAndDialog);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(props.getModel())
                .messages(List.of(system, user))
                .maxTokens(700)
                .temperature(0.5)
                .build();
        String content = openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent().trim();
        return content.isBlank() ? null : content;
    }


}
