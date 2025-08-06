package com.c203.autobiography.domain.episode.template.repository;

import com.c203.autobiography.domain.episode.template.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, String> {
    
    List<Chapter> findAllByOrderByChapterOrderAsc();
    
    Optional<Chapter> findByChapterOrder(int chapterOrder);
    
    @Query("SELECT c FROM Chapter c JOIN FETCH c.templates t WHERE c.chapterId = :chapterId ORDER BY t.templateOrder")
    Optional<Chapter> findByChapterIdWithTemplates(String chapterId);
} 