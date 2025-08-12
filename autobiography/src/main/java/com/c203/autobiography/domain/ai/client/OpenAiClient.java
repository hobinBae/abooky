package com.c203.autobiography.domain.ai.client;

import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatMessage;
import com.c203.autobiography.domain.ai.service.OpenAiService;
import jakarta.annotation.PostConstruct;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenAiClient implements AiClient {
    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);
    private final OpenAiService openAiService;
    private final OpenAiProperties props;

    private Map<String, Function<OpenAiProperties, String>> promptMapping;

    @PostConstruct
    public void init() {
        promptMapping = Map.ofEntries(
                Map.entry("INTRO", OpenAiProperties::getDynUserIntro),
                Map.entry("INTRO_SCENE", OpenAiProperties::getDynUserIntroScene),
                Map.entry("CHILDHOOD_HOME", OpenAiProperties::getDynUserChildhoodHome),
                Map.entry("CHILDHOOD_CAREGIVER", OpenAiProperties::getDynUserChildhoodCaregiver),
                Map.entry("CHILDHOOD_PERSONALITY", OpenAiProperties::getDynUserChildhoodPersonality),
                Map.entry("CHILDHOOD_PLAY", OpenAiProperties::getDynUserChildhoodPlay),
                Map.entry("CHILDHOOD_FAVORITES", OpenAiProperties::getDynUserChildhoodFavorites),
                Map.entry("CHILDHOOD_DREAM_JOB", OpenAiProperties::getDynUserChildhoodDreamJob),
                Map.entry("CHILDHOOD_SCENE", OpenAiProperties::getDynUserChildhoodScene),
                Map.entry("UPPER_ELEM_SCHOOL", OpenAiProperties::getDynUserUpperElemSchool),
                Map.entry("UPPER_ELEM_PEOPLE", OpenAiProperties::getDynUserUpperElemPeople),
                Map.entry("UPPER_ELEM", OpenAiProperties::getDynUserUpperElem),
                Map.entry("UPPER_ELEM_EVENT", OpenAiProperties::getDynUserUpperElemEvent),
                Map.entry("MIDDLE_PUBERTY", OpenAiProperties::getDynUserMiddlePuberty),
                Map.entry("MIDDLE_MEDIA", OpenAiProperties::getDynUserMiddleMedia),
                Map.entry("MIDDLE_PEOPLE", OpenAiProperties::getDynUserMiddlePeople),
                Map.entry("MIDDLE_ACADEMICS", OpenAiProperties::getDynUserMiddleAcademics),
                Map.entry("MIDDLE_GROWTH", OpenAiProperties::getDynUserMiddleGrowth),
                Map.entry("HIGH_ACADEMICS", OpenAiProperties::getDynUserHighAcademics),
                Map.entry("HIGH_HOBBY", OpenAiProperties::getDynUserHighHobby),
                Map.entry("HIGH_FRIEND", OpenAiProperties::getDynUserHighFriend),
                Map.entry("HIGH_EVENT", OpenAiProperties::getDynUserHighEvent),
                Map.entry("HIGH_GROWTH", OpenAiProperties::getDynUserHighGrowth),
                Map.entry("TRANSITION", OpenAiProperties::getDynUserTransition),
                Map.entry("COLLEGE_OR_WORK", OpenAiProperties::getDynUserCollegeOrWork)
        );
    }

    @Override
    public String generateDynamicFollowUpBySection(String sectionKey, String userAnswer) {
        log.info("다이나믹 후속 질문 생성. Key: {}", sectionKey);
        String ua = safe(userAnswer);

        // Map에서 프롬프트 템플릿을 가져옴. 키가 없으면 기본값(INTRO)으로 대체
        String promptTemplate = promptMapping.getOrDefault(sectionKey, OpenAiProperties::getDynUserIntro).apply(props);
        String userPrompt = String.format(promptTemplate, ua);

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

        log.info("DynamicFollowUp 응답: {}", response);
        return response.isBlank() ? null : response;
    }


    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    @Override
    public String generateEpisode(String conversationHistory) {
        ChatMessage system = ChatMessage.of("system", props.getEpisodeGenerationSystem());
        ChatMessage user = ChatMessage.of("user",
                String.format(props.getEpisodeGenerationTemplate(), conversationHistory));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(props.getModel())
                .messages(List.of(system, user))
                .maxTokens(1500) // 에피소드는 더 긴 텍스트 필요
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
        log.info("후속 질문 분석");
        ChatMessage system = ChatMessage.of("system", props.getAnswerAnalysisSystem());
        ChatMessage user = ChatMessage.of("user",
                String.format(props.getAnswerAnalysisTemplate(), userAnswer, remainingQuestions));

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
