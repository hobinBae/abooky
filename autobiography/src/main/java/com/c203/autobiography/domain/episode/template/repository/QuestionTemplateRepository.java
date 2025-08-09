package com.c203.autobiography.domain.episode.template.repository;

import com.c203.autobiography.domain.episode.template.entity.QuestionTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionTemplateRepository extends JpaRepository<QuestionTemplate, Long> {
    Optional<QuestionTemplate> findByTemplateOrder(int templateOrder);
    List<QuestionTemplate> findAllByOrderByTemplateOrderAsc();
}
