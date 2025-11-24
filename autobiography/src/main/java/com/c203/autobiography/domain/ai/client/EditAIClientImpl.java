package com.c203.autobiography.domain.ai.client;

import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatMessage;
import com.c203.autobiography.domain.ai.loader.PromptLoader;
import com.c203.autobiography.domain.ai.prompt.EditPrompts;
import com.c203.autobiography.domain.ai.service.OpenAiService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EditAIClientImpl extends BaseAIClient implements EditAIClient {

    private final PromptLoader promptLoader;
    public EditAIClientImpl(OpenAiService openAiService,
                            com.c203.autobiography.domain.ai.properties.OpenAiProperties props,
                            PromptLoader promptLoader) {
        super(openAiService, props);
        this.promptLoader = promptLoader;
    }

    @Override
    public String editText(String rawText, String tone, String priorContext) {

        // YAML 로드
        EditPrompts prompt = promptLoader.load("edit.yml", EditPrompts.class);

        // tone 템플릿 가져오기
        String toneTemplate = prompt.getTones().get(tone);

        if (toneTemplate == null) {
            log.warn("Unknown edit tone key: {}, fallback to plain", tone);
            toneTemplate = prompt.getTones().get("plain");
        }

        // priorContext 적용
        String finalText = (priorContext != null && !priorContext.isBlank())
                ? priorContext + "\n\n" + rawText
                : rawText;

        // 사용자 메시지 생성
        String userPrompt = String.format(toneTemplate, finalText);

        ChatMessage system = ChatMessage.system(prompt.getSystem());
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
