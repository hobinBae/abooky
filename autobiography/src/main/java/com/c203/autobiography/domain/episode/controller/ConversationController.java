package com.c203.autobiography.domain.episode.controller;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import com.c203.autobiography.domain.episode.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.dto.ConversationMessageUpdateRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionResponse;
import com.c203.autobiography.domain.episode.dto.ConversationSessionUpdateRequest;
import com.c203.autobiography.domain.episode.dto.MessageType;
import com.c203.autobiography.domain.episode.dto.StartConversationResponse;
import com.c203.autobiography.domain.episode.entity.ConversationMessage;
import com.c203.autobiography.domain.episode.entity.ConversationSession;
import com.c203.autobiography.domain.episode.repository.ConversationMessageRepository;
import com.c203.autobiography.domain.episode.service.ConversationMessageService;
import com.c203.autobiography.domain.episode.service.ConversationService;
import com.c203.autobiography.domain.sse.service.SseService;
import com.c203.autobiography.domain.episode.template.dto.QuestionResponse;
import com.c203.autobiography.domain.episode.template.dto.NextQuestionDto;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import retrofit2.http.Path;

@RestController
@RequestMapping("/api/v1/conversation")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ConversationController {
    private final ConversationService conversationService;
    private final ConversationMessageService conversationMessageService;
    private final SseService sseService;

    /**
     * 새로운 대화 시작
     *
     * @return 생성된 세션 ID
     */
    @PostMapping("{bookId}/episodes/{episodeId}/sessions")
    public ResponseEntity<ApiResponse<Map<String, String>>> startConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            @PathVariable Long episodeId,
            HttpServletRequest httpRequest) {
        Long memberId = userDetails.getMemberId();
        String sessionId = conversationService.startNewConversation(memberId, bookId, episodeId);
        Map<String, String> response = Map.of("sessionId", sessionId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "새로운 대화 세션이 생성되었습니다.", response, httpRequest.getRequestURI()));
    }

    /**
     * 지정된 세션에 대한 SSE 스트림을 "연결"하고 대화를 시작/재개합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return SseEmitter 객체
     */
    @GetMapping(value = "{bookId}/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String sessionId, @PathVariable Long bookId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        try {
            conversationService.establishConversationStream(sessionId, bookId, emitter);
        } catch (Exception e) {
            log.error("SSE 스트림 설정 중 에러 발생. sessionId={}", sessionId, e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("스트림 연결 중 서버 오류가 발생했습니다: " + e.getMessage()));
            } catch (IOException ex) {
                log.warn("SSE 에러 이벤트 전송 실패. sessionId={}", sessionId, ex);
            }
            emitter.completeWithError(e);
        }

        return emitter;
    }


    /**
     * 메시지 생성
     */
    @PostMapping("/message")
    public ResponseEntity<ConversationMessageResponse> createMessage(
            @Valid @RequestBody ConversationMessageRequest req
    ) {
        ConversationMessageResponse res = conversationMessageService.createMessage(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("{bookId}/episodes/{episodeId}/next")
    public ResponseEntity<Void> nextQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            @PathVariable Long episodeId,
            @RequestParam String sessionId
    ) {
        Long memberId = userDetails.getMemberId();

        conversationService.proceedToNextQuestion(memberId, bookId, episodeId, sessionId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("{bookId}/episodes/{episodeId}/skip")
    public ResponseEntity<Void> skipQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            @PathVariable Long episodeId,
            @RequestParam String sessionId
    ) {
        Long memberId = userDetails.getMemberId();
        conversationService.skipCurrentQuestion(memberId, bookId, episodeId, sessionId);
        return ResponseEntity.ok().build();
    }


    /**
     * ★★★ SSE 연결 종료를 위한 새로운 API 추가 ★★★ 클라이언트가 페이지를 떠나기 전에 호출하여 서버 측 리소스를 즉시 정리합니다.
     *
     * @param sessionId 종료할 대화 세션 ID
     */
    @DeleteMapping("/stream/{sessionId}")
    public ResponseEntity<Void> closeSseStream(@PathVariable String sessionId) {
        sseService.closeConnection(sessionId);
        return ResponseEntity.ok().build();
    }



}
