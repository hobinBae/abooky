package com.c203.autobiography.domain.episode.template.controller;

import com.c203.autobiography.domain.episode.template.service.ChapterDataInitService;
import com.c203.autobiography.domain.episode.template.service.ChapterBasedQuestionService;
import com.c203.autobiography.domain.episode.template.dto.NextQuestionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chapter")
@RequiredArgsConstructor
@Slf4j
public class ChapterTestController {
    
    private final ChapterDataInitService initService;
    private final ChapterBasedQuestionService questionService;
    
    /**
     * 챕터 데이터 수동 초기화 (테스트용)
     */
    @PostMapping("/init")
    public ResponseEntity<String> initChapterData() {
        try {
            initService.initializeChapterData();
            return ResponseEntity.ok("챕터 데이터 초기화 완료");
        } catch (Exception e) {
            log.error("챕터 데이터 초기화 실패", e);
            return ResponseEntity.badRequest().body("초기화 실패: " + e.getMessage());
        }
    }
    
    /**
     * 특정 세션의 챕터 기반 첫 질문 테스트
     */
    @PostMapping("/test/init")
    public ResponseEntity<NextQuestionDto> testInitSession(@RequestParam String sessionId) {
        try {
            NextQuestionDto result = questionService.initializeSession(sessionId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("세션 초기화 테스트 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 답변 후 다음 질문 테스트
     */
    @PostMapping("/test/next")
    public ResponseEntity<NextQuestionDto> testNextQuestion(
            @RequestParam String sessionId, 
            @RequestParam String userAnswer) {
        try {
            NextQuestionDto result = questionService.getNextQuestion(sessionId, userAnswer);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("다음 질문 테스트 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
} 