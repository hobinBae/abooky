package com.c203.autobiography.domain.ai.client;

import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatMessage;
import com.c203.autobiography.domain.ai.loader.PromptLoader;
import com.c203.autobiography.domain.ai.prompt.FollowUpPrompts;
import com.c203.autobiography.domain.ai.properties.OpenAiProperties;
import com.c203.autobiography.domain.ai.service.OpenAiService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FollowUpAIClientImpl extends BaseAIClient implements FollowUpAIClient {

    private final PromptLoader promptLoader;

    protected FollowUpAIClientImpl(OpenAiService openAiService,
                                   OpenAiProperties props
    ,PromptLoader promptLoader) {
        super(openAiService, props);
        this.promptLoader = promptLoader;
    }

    @Override
    public String generateDynamicFollowUpBySection(String sectionKey, String userAnswer, String nextTemplateQuestion) {

        FollowUpPrompts prompt = promptLoader.load("followup.yml", FollowUpPrompts.class);

        FollowUpPrompts.PromptEntry entry = prompt.getPrompts().get(sectionKey);
        if (entry == null) {
            log.warn("FollowUp prompt not found for key: {}", sectionKey);
            return null;
        }

        String userPrompt = String.format(entry.getUserTemplate(),
                userAnswer,
                nextTemplateQuestion != null ? nextTemplateQuestion : ""
        );


        ChatMessage system = ChatMessage.system(entry.getSystem());
        ChatMessage user = ChatMessage.user(userPrompt);

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
