package com.c203.autobiography.domain.template.service;

import com.c203.autobiography.domain.template.entity.QuestionTemplate;
import com.c203.autobiography.domain.template.repository.QuestionTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionTemplateServiceImpl implements QuestionTemplateService{
    private final QuestionTemplateRepository templateRepo;
    @Override
    public String getByOrder(int idx) {
        return templateRepo
                .findByTemplateOrder(idx)
                .map(QuestionTemplate::getTemplateText)
                .orElse(null);

    }
}
