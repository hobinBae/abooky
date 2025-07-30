package com.c203.autobiography.domain.episode.conversation.controller;

import com.c203.autobiography.domain.episode.conversation.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationMessageUpdateRequest;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationSessionRequest;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationSessionResponse;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationSessionUpdateRequest;
import com.c203.autobiography.domain.episode.conversation.service.ConversationService;
import com.c203.autobiography.domain.sse.service.SseService;
import com.c203.autobiography.domain.template.dto.QuestionResponse;
import com.c203.autobiography.domain.template.service.QuestionTemplateService;
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
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter startConversation(@Valid @RequestParam String sessionId,
                                        @Valid @RequestParam Long episodeId) {
        convService.createSession(new ConversationSessionRequest(sessionId, episodeId));
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onTimeout(() -> sseService.remove(sessionId));
        emitter.onCompletion(() -> sseService.remove(sessionId));
        sseService.register(sessionId, emitter);
        String first = templateService.getByOrder(0);
        sseService.pushQuestion(sessionId, QuestionResponse.builder().text(first).build());
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
