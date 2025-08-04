package com.c203.autobiography.domain.episode.template.service;

import java.util.List;

public interface QuestionTemplateService {
    public List<String> getAllInorder();
    public String getByOrder(int idx);
}
