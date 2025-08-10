package com.c203.autobiography.domain.episode.template.service;

import com.c203.autobiography.domain.episode.template.dto.ChapterProgressDto;
import com.c203.autobiography.domain.episode.template.dto.NextQuestionDto;

import java.util.List;

public interface ChapterBasedQuestionService {
    
    /**
     * 세션 초기화 및 첫 번째 질문 반환
     */
    NextQuestionDto initializeSession(String sessionId);
    
    /**
     * 사용자 답변을 기반으로 다음 질문 생성
     * - static 후속질문이 있으면 순서대로 반환
     * - dynamic 타입이면 AI로 후속질문 생성
     * - 모든 후속질문이 끝나면 다음 템플릿의 메인 질문으로 이동
     */
    NextQuestionDto getNextQuestion(String sessionId, String userAnswer);
    
    /**
     * 현재 세션의 진행 상황 조회
     */
    ChapterProgressDto getSessionProgress(String sessionId);
    
    /**
     * 특정 챕터의 모든 템플릿 조회
     */
    List<String> getChapterTemplates(String chapterId);
    
    /**
     * 세션 상태 초기화 (특정 챕터부터 다시 시작)
     */
    void resetSessionToChapter(String sessionId, String chapterId);
    
    /**
     * 현재 답변 분량이 에피소드 생성 기준에 도달했는지 확인
     */
    boolean shouldCreateEpisode(String sessionId);
    
    /**
     * 에피소드 생성을 위한 대화 내용 정리
     */
    String generateEpisodeContent(String sessionId);


} 