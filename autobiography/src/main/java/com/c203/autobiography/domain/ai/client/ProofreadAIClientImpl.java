package com.c203.autobiography.domain.ai.client;

import com.c203.autobiography.domain.ai.dto.ChatCompletionRequest;
import com.c203.autobiography.domain.ai.dto.ChatMessage;
import com.c203.autobiography.domain.ai.loader.PromptLoader;
import com.c203.autobiography.domain.ai.prompt.ProofreadPrompts;
import com.c203.autobiography.domain.ai.properties.OpenAiProperties;
import com.c203.autobiography.domain.ai.service.OpenAiService;
import com.c203.autobiography.domain.book.repository.BookCategoryRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProofreadAIClientImpl extends BaseAIClient implements ProofreadAIClient {

    private final BookCategoryRepository bookCategoryRepository;
    private final PromptLoader promptLoader;

    protected ProofreadAIClientImpl(OpenAiService openAiService,
                                    OpenAiProperties props, BookCategoryRepository bookCategoryRepository,
                                    PromptLoader promptLoader) {
        super(openAiService, props);
        this.bookCategoryRepository = bookCategoryRepository;
        this.promptLoader = promptLoader;
    }


    @Override
    public String proofread(String originalText, Long categoryId) {

        var category = bookCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));

        ProofreadPrompts prompt = promptLoader.load("proofread.yml", ProofreadPrompts.class);

        String userPrompt = String.format(prompt.getTemplate(),
                category.getCategoryName(),
                originalText);

        ChatCompletionRequest request =
                ChatCompletionRequest.builder()
                        .model(props.getModel())
                        .messages(List.of(
                                ChatMessage.system(prompt.getSystem()),
                                ChatMessage.user(userPrompt)
                        ))
                        .maxTokens(prompt.getMaxTokens())
                        .temperature(prompt.getTemperature())
                        .build();

        return call(request);
    }
}
