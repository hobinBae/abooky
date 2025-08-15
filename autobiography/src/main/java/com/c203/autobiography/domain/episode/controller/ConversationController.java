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
    private final SseService sseService;
    private final ConversationMessageRepository conversationMessageRepository;

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
     * @param sessionId 대화 세션 ID
     * @return SseEmitter 객체
     */
    @GetMapping(value = "{bookId}/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String sessionId, @PathVariable Long bookId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        try {
            // ★★★ 2. 기존 서비스 호출 로직을 try 블록 안으로 옮깁니다. ★★★
            // 이 때, 생성한 emitter 객체를 서비스로 전달하여 등록하도록 시그니처를 변경해야 합니다.
            conversationService.establishConversationStream(sessionId, bookId, emitter);
        } catch (Exception e) {
            // ★★★ 3. 서비스 로직에서 예외가 발생하면 여기서 잡습니다. ★★★
            log.error("SSE 스트림 설정 중 에러 발생. sessionId={}", sessionId, e);
            try {
                // 클라이언트에게 'error'라는 이름의 이벤트를 보냅니다.
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("스트림 연결 중 서버 오류가 발생했습니다: " + e.getMessage()));
            } catch (IOException ex) {
                log.warn("SSE 에러 이벤트 전송 실패. sessionId={}", sessionId, ex);
            }
            // 에러와 함께 스트림을 완전히 종료시킵니다.
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 세션 업데이트
     */
    @PutMapping("/session")

    public ResponseEntity<ConversationSessionResponse> updateSession(
            @Valid @RequestBody ConversationSessionUpdateRequest req) {
        return ResponseEntity.ok(conversationService.updateSession(req));
    }

    /**
     * 세션 조회
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ConversationSessionResponse> getSession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(conversationService.getSession(sessionId));
    }

    /**
     * 메시지 수정
     */
    @PutMapping("/message")
    public ResponseEntity<ConversationMessageResponse> updateMessage(
            @Valid @RequestBody ConversationMessageUpdateRequest req) {
        return ResponseEntity.ok(conversationService.updateMessage(req));
    }

    /**
     * 메시지 생성
     */
    @PostMapping("/message")
    public ResponseEntity<ConversationMessageResponse> createMessage(
            @Valid @RequestBody ConversationMessageRequest req
    ) {
        ConversationMessageResponse res = conversationService.createMessage(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * 메시지 히스토리 조회
     */
    @GetMapping("/{sessionId}/history")
    public ResponseEntity<List<ConversationMessageResponse>> getHistory(
            @PathVariable String sessionId) {

        return ResponseEntity.ok(conversationService.getHistory(sessionId));
    }

    @PostMapping("{bookId}/episodes/{episodeId}/next")
    public ResponseEntity<Void> nextQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            @PathVariable Long episodeId,
            @RequestParam String sessionId
    ) {
        // 1) 서비스에서 다음 질문 꺼내기 - 챕터 기반 모드 우선 시도
        ConversationSession session = conversationService.getSessionEntity(sessionId);
        NextQuestionDto nextQuestionDto = null;
        String next = null;
        Long memberId = userDetails.getMemberId();

        if (session != null && session.getCurrentChapterId() != null) {
            // 챕터 기반 모드
            try {
                String lastAnswer = conversationService.getLastAnswer(sessionId);
                nextQuestionDto = conversationService.getNextQuestion(memberId, bookId, episodeId ,sessionId, lastAnswer);
                if (nextQuestionDto == null) {
                    log.info("서비스에서 모든 처리를 완료하고 null을 반환했습니다. 컨트롤러는 즉시 종료합니다. SessionId: {}", sessionId);
                    return ResponseEntity.ok().build();
                }
                next = nextQuestionDto.getQuestionText();

            } catch (Exception e) {
                // 오류시 레거시 모드로 fallback
                e.printStackTrace();
                return ResponseEntity.internalServerError().build();
            }
        } else {
            log.error("챕터 기반 대화가 시작되지 않은 세션입니다. SessionId: {}", sessionId);
            return ResponseEntity.badRequest().build();
        }

        if (next != null) {
            // 2) DB에 QUESTION 메시지 저장
            conversationService.createMessage(
                    ConversationMessageRequest.builder()
                            .sessionId(sessionId)
                            .messageType(MessageType.QUESTION)
                            .content(next)
                            .build()
            );

            // 3) SSE로 클라이언트에 푸시 (챕터 정보 포함)
            QuestionResponse.QuestionResponseBuilder responseBuilder = QuestionResponse.builder()
                    .text(next);

            if (nextQuestionDto != null) {
                responseBuilder
                        .currentChapter(nextQuestionDto.getCurrentChapterName())
                        .currentStage(nextQuestionDto.getCurrentStageName())
                        .questionType(nextQuestionDto.getQuestionType())
                        .overallProgress(nextQuestionDto.getOverallProgress())
                        .chapterProgress(nextQuestionDto.getChapterProgress())
                        .isLastQuestion(nextQuestionDto.isLastQuestion());
            }

            sseService.pushQuestion(sessionId, responseBuilder.build());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * ★★★ SSE 연결 종료를 위한 새로운 API 추가 ★★★
     * 클라이언트가 페이지를 떠나기 전에 호출하여 서버 측 리소스를 즉시 정리합니다.
     * @param sessionId 종료할 대화 세션 ID
     */
    @DeleteMapping("/stream/{sessionId}")
    public ResponseEntity<Void> closeSseStream(@PathVariable String sessionId) {
        sseService.closeConnection(sessionId);
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

    private Integer computeEpisodeStartNo(String sessionId) {
        // 세션 시작 시점에 포함할 구간 시작점: 현재 최대 messageNo + 1 (기록이 없으면 1)
        Integer maxNo = conversationMessageRepository.findMaxMessageNo(sessionId);
        return (maxNo == null) ? 1 : (maxNo + 1);
    }

}
