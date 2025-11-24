package com.c203.autobiography.domain.ai.client;


import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatMessage;
import com.c203.autobiography.domain.ai.loader.PromptLoader;
import com.c203.autobiography.domain.ai.prompt.AnalysisPrompts;
import com.c203.autobiography.domain.ai.properties.OpenAiProperties;
import com.c203.autobiography.domain.ai.service.OpenAiService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnswerAnalysisAIClientImpl extends BaseAIClient implements AnswerAnalysisAIClient {

    private final PromptLoader promptLoader;

    protected AnswerAnalysisAIClientImpl(OpenAiService openAiService,
                                         OpenAiProperties props, PromptLoader promptLoader) {
        super(openAiService, props);
        this.promptLoader = promptLoader;
    }

    @Override
    public String analyzeAnsweredQuestions(String userAnswer, String remainingQuestions) {

        AnalysisPrompts prompt = promptLoader.load("analysis.yml", AnalysisPrompts.class);


        ChatMessage system = ChatMessage.system(prompt.getSystem());
        ChatMessage user = ChatMessage.user(
                String.format(prompt.getTemplate(), userAnswer, remainingQuestions)
        );


        ChatCompletionRequest request =
                ChatCompletionRequest.builder()
                        .model(props.getModel())
                        .messages(List.of(system, user))
                        .maxTokens(prompt.getMaxTokens())
                        .temperature(prompt.getTemperature())
                        .build();

        return call(request);
    }
}
