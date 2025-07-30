package com.c203.autobiography.domain.template.repository;

import com.c203.autobiography.domain.template.entity.QuestionTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionTemplateRepository extends JpaRepository<QuestionTemplate, Long> {
    Optional<QuestionTemplate> findByTemplateOrder(int templateOrder);
}
