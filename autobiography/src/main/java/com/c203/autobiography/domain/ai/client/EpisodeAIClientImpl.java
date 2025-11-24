package com.c203.autobiography.domain.ai.client;

import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatMessage;
import com.c203.autobiography.domain.ai.loader.PromptLoader;
import com.c203.autobiography.domain.ai.prompt.EpisodePrompts;
import com.c203.autobiography.domain.ai.properties.OpenAiProperties;
import com.c203.autobiography.domain.ai.service.OpenAiService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EpisodeAIClientImpl extends BaseAIClient implements EpisodeAIClient {

    private final PromptLoader promptLoader;

    protected EpisodeAIClientImpl(OpenAiService openAiService,
                                  OpenAiProperties props, PromptLoader promptLoader) {
        super(openAiService, props);
        this.promptLoader = promptLoader;
    }

    @Override
    public String generateEpisode(String chapterId, String dialog, boolean jsonMode) {

        EpisodePrompts prompt = promptLoader.load("episode.yml", EpisodePrompts.class);
        EpisodePrompts.PromptEntry entry = prompt.getPrompts()
                .getOrDefault(chapterId, prompt.getPrompts().get("chapter1"));

        if (entry == null) {
            throw new IllegalStateException("No episode prompt for chapterId: " + chapterId);
        }

        ChatMessage system = ChatMessage.system(entry.getSystem());
        ChatMessage user = ChatMessage.user(
                String.format(entry.getUserTemplate(), dialog)
        );

        ChatCompletionRequest.ChatCompletionRequestBuilder builder =
                ChatCompletionRequest.builder()
                        .model(props.getModel())
                        .messages(List.of(system, user))
                        .maxTokens(prompt.getMaxTokens())
                        .temperature(prompt.getTemperature());

        if (jsonMode) {
            builder.responseFormat(new ChatCompletionRequest.ResponseFormat("json_object"));
        }

        return call(builder.build());
    }
}
