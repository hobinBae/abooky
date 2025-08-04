package com.c203.autobiography.domain.episode.template.service;

import com.c203.autobiography.domain.episode.template.entity.QuestionTemplate;
import com.c203.autobiography.domain.episode.template.repository.QuestionTemplateRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionTemplateServiceImpl implements QuestionTemplateService{
    private final QuestionTemplateRepository templateRepo;

    @Override
    public List<String> getAllInorder() {
        return templateRepo
                .findAllByOrderByTemplateOrderAsc()
                .stream()
                .map(QuestionTemplate::getTemplateText)
                .collect(Collectors.toList());
    }

    @Override
    public String getByOrder(int idx) {
        return templateRepo
                .findByTemplateOrder(idx)
                .map(QuestionTemplate::getTemplateText)
                .orElse(null);

    }
}
