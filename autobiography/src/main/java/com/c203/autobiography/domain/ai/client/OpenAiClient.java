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

    @Override
    public String editText(String rawAnswer, String priorContext, String tone) {
        ChatMessage system = ChatMessage.of("system", props.getTextEditSystem());

        String userPrompt = buildEditPrompt(rawAnswer, priorContext, tone);
        ChatMessage user = ChatMessage.of("user", userPrompt);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(props.getModel())
                .messages(List.of(system, user))
                .maxTokens(props.getMaxTokensEdit())
                .temperature(props.getEditTemperature())
                .build();

        String editedText = openAiService.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent()
                .trim();

        return editedText.isBlank() ? rawAnswer : editedText;
    }

    private String buildEditPrompt(String rawAnswer, String priorContext, String tone) {
        StringBuilder prompt = new StringBuilder();

        if(priorContext != null && !priorContext.isBlank()) {
            prompt.append("이전 맥락:\n").append(priorContext).append("\n\n");
        }
        prompt.append("편집할 텍스트:\n").append(rawAnswer).append("\n\n");

        switch(tone.toUpperCase()) {
            case "FORMAL" -> prompt.append(props.getToneFormal());
            case "CASUAL" -> prompt.append(props.getToneCasual());
            case "EMOTIONAL" -> prompt.append(props.getToneEmotional());
            default -> prompt.append(props.getTonePlain());
        }
        return prompt.toString();
    }


}
