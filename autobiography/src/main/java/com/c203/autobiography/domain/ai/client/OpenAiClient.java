package com.c203.autobiography.domain.ai.client;

import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatMessage;
import com.c203.autobiography.domain.ai.service.OpenAiService;
import com.c203.autobiography.domain.book.entity.BookCategory;
import com.c203.autobiography.domain.book.repository.BookCategoryRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
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
    private final BookCategoryRepository bookCategoryRepository;

    private Map<String, Function<OpenAiProperties, String>> promptMapping;

//    @PostConstruct
//    public void init() {
//        promptMapping = Map.ofEntries(
//                Map.entry("INTRO", OpenAiProperties::getDynUserIntro),
//                Map.entry("INTRO_SCENE", OpenAiProperties::getDynUserIntroScene),
//                Map.entry("CHILDHOOD_HOME", OpenAiProperties::getDynUserChildhoodHome),
//                Map.entry("CHILDHOOD_CAREGIVER", OpenAiProperties::getDynUserChildhoodCaregiver),
//                Map.entry("CHILDHOOD_PERSONALITY", OpenAiProperties::getDynUserChildhoodPersonality),
//                Map.entry("CHILDHOOD_PLAY", OpenAiProperties::getDynUserChildhoodPlay),
//                Map.entry("CHILDHOOD_FAVORITES", OpenAiProperties::getDynUserChildhoodFavorites),
//                Map.entry("CHILDHOOD_DREAM_JOB", OpenAiProperties::getDynUserChildhoodDreamJob),
//                Map.entry("CHILDHOOD_SCENE", OpenAiProperties::getDynUserChildhoodScene),
//                Map.entry("UPPER_ELEM_SCHOOL", OpenAiProperties::getDynUserUpperElemSchool),
//                Map.entry("UPPER_ELEM_PEOPLE", OpenAiProperties::getDynUserUpperElemPeople),
//                Map.entry("UPPER_ELEM", OpenAiProperties::getDynUserUpperElem),
//                Map.entry("UPPER_ELEM_EVENT", OpenAiProperties::getDynUserUpperElemEvent),
//                Map.entry("MIDDLE_PUBERTY", OpenAiProperties::getDynUserMiddlePuberty),
//                Map.entry("MIDDLE_MEDIA", OpenAiProperties::getDynUserMiddleMedia),
//                Map.entry("MIDDLE_PEOPLE", OpenAiProperties::getDynUserMiddlePeople),
//                Map.entry("MIDDLE_ACADEMICS", OpenAiProperties::getDynUserMiddleAcademics),
//                Map.entry("MIDDLE_GROWTH", OpenAiProperties::getDynUserMiddleGrowth),
//                Map.entry("HIGH_ACADEMICS", OpenAiProperties::getDynUserHighAcademics),
//                Map.entry("HIGH_HOBBY", OpenAiProperties::getDynUserHighHobby),
//                Map.entry("HIGH_FRIEND", OpenAiProperties::getDynUserHighFriend),
//                Map.entry("HIGH_EVENT", OpenAiProperties::getDynUserHighEvent),
//                Map.entry("HIGH_GROWTH", OpenAiProperties::getDynUserHighGrowth),
//                Map.entry("TRANSITION", OpenAiProperties::getDynUserTransition),
//                Map.entry("COLLEGE_OR_WORK", OpenAiProperties::getDynUserCollegeOrWork)
//        );
//    }

    @Override
    public String generateDynamicFollowUpBySection(String sectionKey, String userAnswer, String nextTemplateQuestion) {
        log.info("다이나믹 후속 질문 생성. Key: {}, NextQ 제공 여부: {}", sectionKey, nextTemplateQuestion != null);

        // 1. 새로운 프롬프트 맵에서 sectionKey에 해당하는 Prompt 객체를 가져옵니다.
        OpenAiProperties.Prompt prompt = props.getDynamicPrompts().get(sectionKey);

        // 2. 키에 해당하는 프롬프트가 없으면 경고 후 null 반환
        if (prompt == null) {
            log.warn("정의되지 않은 다이나믹 프롬프트 Key입니다: {}", sectionKey);
            return null;
        }

        // 3. Prompt 객체에서 시스템 메시지와 사용자 프롬프트 템플릿을 가져옵니다.
        String systemMessage = prompt.system();
        String userPromptTemplate = prompt.userTemplate();
        String userPrompt;

        // 4. '다음 본 질문' 파라미터의 유무에 따라 사용자 프롬프트를 완성합니다.
        if (nextTemplateQuestion != null && !nextTemplateQuestion.isBlank()) {
            // '다음 본 질문'이 있으면 2개의 인자로 포맷팅
            userPrompt = String.format(userPromptTemplate, safe(userAnswer), nextTemplateQuestion);
        } else {
            // 없으면 기존처럼 1개의 인자로 포맷팅
            userPrompt = String.format(userPromptTemplate, safe(userAnswer));
        }

        // 5. API 요청 메시지 생성
        ChatMessage system = ChatMessage.of("system", systemMessage);
        ChatMessage user = ChatMessage.of("user", userPrompt);

        // 6. OpenAI API 요청 및 응답 반환
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


//    @Override
//    public String generateEpisode(String prompt, boolean jsonMode) {
//        ChatMessage system = ChatMessage.of("system", props.getEpisodeGenerationSystem());
//        ChatMessage user = ChatMessage.of("user", prompt); // EpisodeServiceImpl에서 이미 포맷팅된 프롬프트를 받음
//
//        // 1. 요청 빌더를 먼저 생성합니다.
//        ChatCompletionRequest.ChatCompletionRequestBuilder requestBuilder = ChatCompletionRequest.builder()
//                .model(props.getModel())
//                .messages(List.of(system, user))
//                .maxTokens(1500)
//                .temperature(0.8);
//
//        // 2. [핵심] jsonMode가 true일 때만 responseFormat을 설정합니다.
//        if (jsonMode) {
//            log.info("JSON 모드를 활성화하여 API를 호출합니다.");
//            requestBuilder.responseFormat(new ChatCompletionRequest.ResponseFormat("json_object"));
//        }
//
//        // 3. 최종 요청 객체를 빌드합니다.
//        ChatCompletionRequest request = requestBuilder.build();
//
//        // 4. API 호출 및 결과 반환 (기존과 동일)
//        String episode = openAiService.createChatCompletion(request)
//                .getChoices()
//                .get(0)
//                .getMessage()
//                .getContent()
//                .trim();
//
//        return episode.isBlank() ? null : episode;
//    }

@Override
public String generateEpisode(String chapterId, String dialog, boolean jsonMode) {
    // 1. [핵심] chapterId를 이용해 챕터별 맞춤 프롬프트를 가져옵니다.
    OpenAiProperties.Prompt episodePrompt = props.getEpisodePrompts().get(chapterId);

    // 만약 해당 chapterId의 프롬프트가 없으면, 가장 일반적인 chapter1 프롬프트를 기본값으로 사용합니다.
    if (episodePrompt == null) {
        log.warn("'{}'에 해당하는 에피소드 프롬프트가 없어 기본(chapter1) 프롬프트로 대체합니다.", chapterId);
        episodePrompt = props.getEpisodePrompts().get("chapter1");
        if (episodePrompt == null) { // chapter1마저도 없는 비상상황 대비
            throw new IllegalStateException("기본 에피소드 생성 프롬프트(chapter1)를 찾을 수 없습니다.");
        }
    }

    // 2. 가져온 프롬프트를 사용하여 시스템 메시지와 사용자 메시지를 구성합니다.
    ChatMessage system = ChatMessage.of("system", episodePrompt.system());
    // commonUserTemplate에 대화 내용을 삽입합니다.
    ChatMessage user = ChatMessage.of("user", String.format(episodePrompt.userTemplate(), dialog));

    // 3. API 요청 빌더 생성 (기존 로직과 유사)
    ChatCompletionRequest.ChatCompletionRequestBuilder requestBuilder = ChatCompletionRequest.builder()
            .model(props.getModel())
            .messages(List.of(system, user))
            .maxTokens(1500) // 에피소드는 충분한 길이 필요
            .temperature(0.8); // 창의성을 위해 약간 높게 설정

    // 4. JSON 모드 설정 (기존과 동일)
    if (jsonMode) {
        log.info("JSON 모드를 활성화하여 API를 호출합니다. (Chapter: {})", chapterId);
        requestBuilder.responseFormat(new ChatCompletionRequest.ResponseFormat("json_object"));
    }

    ChatCompletionRequest request = requestBuilder.build();

    // 5. API 호출 및 결과 반환 (기존과 동일)
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


    @Override
    public String proofread(String originalText, Long categoryId) { // ★★★ 파라미터 타입 변경 ★★★
        // 1. ID를 이용해 DB에서 카테고리 엔티티를 조회합니다.
        BookCategory category = bookCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));

        // 2. 조회한 엔티티에서 카테고리 이름(예: "일기", "스포츠")을 가져옵니다.
        String categoryName = category.getCategoryName();

        // 3. 시스템 메시지 설정
        ChatMessage system = ChatMessage.of("system", props.getProofreadSystem());

        // 4. 사용자 메시지 생성 (조회한 categoryName 사용)
        String userPrompt = String.format(props.getProofreadTemplate(), categoryName, originalText);
        ChatMessage user = ChatMessage.of("user", userPrompt);

        // 5. ChatCompletion 요청 객체 생성 (기존과 동일)
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(props.getModel())
                .messages(List.of(system, user))
                .maxTokens(props.getMaxTokensProofread())
                .temperature(props.getTemperatureProofread())
                .build();

        // 6. OpenAI API 호출 및 결과 추출 (기존과 동일)
        String correctedText = openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent().trim();

        return correctedText.isBlank() ? originalText : correctedText;
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
