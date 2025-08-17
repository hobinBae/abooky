package com.c203.autobiography.domain.sse.service;

import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.stt.dto.TranscriptResponse;
import com.c203.autobiography.domain.episode.template.dto.QuestionResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 기반으로 AI 질문, STT 결과, 에피소드 완성 등을 클라이언트에 푸시하기 위한 서비스 인터페이스
 */
public interface SseService {

    /**
     * 특정 세션에 SseEmitter 등록
     */
    void register(String sessionId, SseEmitter emitter);

    /**
     * 세션에서 SseEmitter 제거
     */
    void remove(String sessionId);

    /**
     * AI 질문 푸시
     */
    void pushQuestion(String sessionId, QuestionResponse response);

    /**
     * 부분 인식 결과 푸시
     */
    void pushPartialTranscript(String sessionId, TranscriptResponse response);

    /**
     * 최종 인식 결과 푸시
     */
    void pushFinalTranscript(String sessionId, TranscriptResponse response);

    /**
     * 에피소드 완성 푸시
     */
    void pushEpisode(String sessionId, EpisodeResponse response);

    void closeConnection(String sessionId);

}
