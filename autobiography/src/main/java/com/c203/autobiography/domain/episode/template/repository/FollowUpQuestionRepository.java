package com.c203.autobiography.domain.episode.template.repository;

import com.c203.autobiography.domain.episode.template.entity.FollowUpQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowUpQuestionRepository extends JpaRepository<FollowUpQuestion, Long> {
    
    List<FollowUpQuestion> findByChapterTemplateTemplateIdOrderByQuestionOrderAsc(String templateId);
} 