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
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        return conversationService.establishConversationStream(sessionId, bookId);
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

    private Integer computeEpisodeStartNo(String sessionId) {
        // 세션 시작 시점에 포함할 구간 시작점: 현재 최대 messageNo + 1 (기록이 없으면 1)
        Integer maxNo = conversationMessageRepository.findMaxMessageNo(sessionId);
        return (maxNo == null) ? 1 : (maxNo + 1);
    }
}
