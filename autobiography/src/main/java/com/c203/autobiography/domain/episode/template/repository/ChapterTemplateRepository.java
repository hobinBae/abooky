package com.c203.autobiography.domain.episode.template.repository;

import com.c203.autobiography.domain.episode.template.entity.ChapterTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChapterTemplateRepository extends JpaRepository<ChapterTemplate, String> {
    
    List<ChapterTemplate> findByChapterChapterIdOrderByTemplateOrderAsc(String chapterId);
    
    @Query("SELECT ct FROM ChapterTemplate ct JOIN FETCH ct.staticFollowUpQuestions f WHERE ct.templateId = :templateId ORDER BY f.questionOrder")
    Optional<ChapterTemplate> findByTemplateIdWithFollowUps(String templateId);
    
    @Query("SELECT ct FROM ChapterTemplate ct " +
           "JOIN ct.chapter c " +
           "WHERE c.chapterOrder = :chapterOrder AND ct.templateOrder = :templateOrder")
    Optional<ChapterTemplate> findByChapterOrderAndTemplateOrder(int chapterOrder, int templateOrder);
} 