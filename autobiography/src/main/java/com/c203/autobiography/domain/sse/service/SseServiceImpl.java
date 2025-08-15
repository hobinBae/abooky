package com.c203.autobiography.domain.sse.service;

import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.stt.dto.TranscriptResponse;
import com.c203.autobiography.domain.episode.template.dto.QuestionResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 서비스 구현체: 세션별 SseEmitter를 관리하고, 다양한 이벤트를 JSON 형태로 클라이언트에 푸시합니다.
 */
@Service
@Slf4j
@EnableScheduling
public class SseServiceImpl implements SseService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public void register(String sessionId, SseEmitter emitter) {
        // 이전 잔재 정리 후 등록
        closeConnection(sessionId);
        emitters.put(sessionId, emitter);

        // 자동 정리 훅
        emitter.onTimeout(() -> closeConnection(sessionId));
        emitter.onCompletion(() -> closeConnection(sessionId));
        emitter.onError(ex -> closeConnection(sessionId));

        // 일부 프록시 활성화를 위한 오픈 코멘트(선택)
        sendSilently(sessionId, emitter, SseEmitter.event().comment("stream-open"));

        log.info("[SSE] Registered emitter for sessionId={}", sessionId);
    }

    @Override
    public void remove(String sessionId) {
        SseEmitter e = emitters.remove(sessionId);
        if (e != null) {
            try {
                e.complete();
            } catch (Exception ignore) {
            }
        }
        log.info("[SSE] Removed emitter for sessionId={}", sessionId);
    }

    @Override
    public void pushQuestion(String sessionId, QuestionResponse response) {
        sendEvent(sessionId, "question", response);
    }

    @Override
    public void pushPartialTranscript(String sessionId, TranscriptResponse response) {
        sendEvent(sessionId, "partialTranscript", response);
    }

    @Override
    public void pushFinalTranscript(String sessionId, TranscriptResponse response) {
        sendEvent(sessionId, "finalTranscript", response);
    }

    @Override
    public void pushEpisode(String sessionId, EpisodeResponse response) {
        sendEvent(sessionId, "episode", response);
    }

    @Override
    public void closeConnection(String sessionId) {
        SseEmitter e = emitters.remove(sessionId);
        if (e != null) {
            try {
                e.complete();
            } catch (Exception ignore) {
            }
            log.info("[SSE] Connection closed. sessionId={}", sessionId);
        }
    }


    /**
     * 공통 이벤트 전송 로직
     *
     * @param sessionId 세션 식별자
     * @param eventName 이벤트 이름
     * @param payload   전송할 데이터 객체
     */
    private void sendEvent(String sessionId, String eventName, Object payload) {
        SseEmitter emitter = emitters.get(sessionId);
        if (emitter == null) {
            log.warn("[SSE] No emitter found for sessionId={}", sessionId);
            return;
        }
        sendSilently(sessionId, emitter,
                SseEmitter.event()
                        .id(eventName + '-' + System.currentTimeMillis())
                        .name(eventName)
                        .data(payload, MediaType.APPLICATION_JSON)
        );
        log.info("[SSE] Pushed event='{}' for sessionId={}", eventName, sessionId);
    }

    @Scheduled(fixedRate = 15_000)
    public void heartbeat() {
        emitters.forEach((id, emitter) ->
                sendSilently(id, emitter, SseEmitter.event().comment("ping"))
        );
    }

    // ✅ 공통 안전 전송: 완료/끊김/예외 시 자동 제거
    private void sendSilently(String sessionId, SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            synchronized (emitter) {
                emitter.send(event);
            }
        } catch (IllegalStateException | IOException ex) { // 이미 complete 또는 전송 실패
            log.debug("[SSE] dead/broken emitter → remove. sessionId={}", sessionId, ex);
            closeConnection(sessionId);
        } catch (Throwable t) {
            log.warn("[SSE] unexpected send error → remove. sessionId={}", sessionId, t);
            closeConnection(sessionId);
        }
    }
}
