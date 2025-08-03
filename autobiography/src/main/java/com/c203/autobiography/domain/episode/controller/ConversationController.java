package com.c203.autobiography.domain.episode.controller;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import com.c203.autobiography.domain.episode.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.dto.ConversationMessageUpdateRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionResponse;
import com.c203.autobiography.domain.episode.dto.ConversationSessionUpdateRequest;
import com.c203.autobiography.domain.episode.dto.MessageType;
import com.c203.autobiography.domain.episode.service.ConversationService;
import com.c203.autobiography.domain.sse.service.SseService;
import com.c203.autobiography.domain.episode.template.dto.QuestionResponse;
import com.c203.autobiography.domain.episode.template.service.QuestionTemplateService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
@Validated
public class ConversationController {
    private final ConversationService convService;
    private final SseService sseService;
    private final QuestionTemplateService templateService;

    /**
     * 에피소드 시작 및 SSE 연결
     */
    @GetMapping(value = "/stream", produces = TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String sessionId) {
        convService.createSession(new ConversationSessionRequest(sessionId, null));
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onTimeout(() -> sseService.remove(sessionId));
        emitter.onCompletion(() -> sseService.remove(sessionId));
        emitter.onError(e -> sseService.remove(sessionId));
        sseService.register(sessionId, emitter);
        // 3) 첫 질문: templateService가 아니라 service.getNextQuestion()으로 꺼내기
        String first = convService.getNextQuestion(sessionId);
        if (first != null) {
            // DB 저장(QUESTION 메시지)도 여기서 해주면 추적이 명확해집니다.
            convService.createMessage(
                    ConversationMessageRequest.builder()
                            .sessionId(sessionId)
                            .messageType(MessageType.QUESTION)
                            .content(first)
                            .build()
            );
            sseService.pushQuestion(
                    sessionId,
                    QuestionResponse.builder().text(first).build()
            );
        }

        return emitter;
    }

    /**
     * 세션 업데이트
     */
    @PutMapping("/session")
    public ResponseEntity<ConversationSessionResponse> updateSession(
            @Valid @RequestBody ConversationSessionUpdateRequest req) {
        return ResponseEntity.ok(convService.updateSession(req));
    }

    /**
     * 세션 조회
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ConversationSessionResponse> getSession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(convService.getSession(sessionId));
    }

    /**
     * 메시지 생성
     */
    @PostMapping("/message")
    public ResponseEntity<ConversationMessageResponse> createMessage(
            @Valid @RequestBody ConversationMessageRequest req) {
        return ResponseEntity.ok(convService.createMessage(req));
    }

    /**
     * 메시지 수정
     */
    @PutMapping("/message")
    public ResponseEntity<ConversationMessageResponse> updateMessage(
            @Valid @RequestBody ConversationMessageUpdateRequest req) {
        return ResponseEntity.ok(convService.updateMessage(req));
    }

    /**
     * 메시지 히스토리 조회
     */
    @GetMapping("/{sessionId}/history")
    public ResponseEntity<List<ConversationMessageResponse>> getHistory(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(convService.getHistory(sessionId));
    }
}
