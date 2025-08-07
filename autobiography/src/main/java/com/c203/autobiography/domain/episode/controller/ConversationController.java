package com.c203.autobiography.domain.episode.controller;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import com.c203.autobiography.domain.episode.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.dto.ConversationMessageUpdateRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionResponse;
import com.c203.autobiography.domain.episode.dto.ConversationSessionUpdateRequest;
import com.c203.autobiography.domain.episode.dto.MessageType;
import com.c203.autobiography.domain.episode.entity.ConversationSession;
import com.c203.autobiography.domain.episode.service.ConversationService;
import com.c203.autobiography.domain.sse.service.SseService;
import com.c203.autobiography.domain.episode.template.dto.QuestionResponse;
import com.c203.autobiography.domain.episode.template.service.QuestionTemplateService;
import com.c203.autobiography.domain.episode.template.service.ChapterBasedQuestionService;
import com.c203.autobiography.domain.episode.template.dto.NextQuestionDto;
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
    private final ChapterBasedQuestionService chapterBasedService;

    /**
     * 에피소드 시작 및 SSE 연결
     */
    @GetMapping(value = "/stream", produces = TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String sessionId, 
                            @RequestParam(value = "useChapterMode", defaultValue = "true") boolean useChapterMode) {
        convService.createSession(new ConversationSessionRequest(sessionId, null));
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onTimeout(() -> sseService.remove(sessionId));
        emitter.onCompletion(() -> sseService.remove(sessionId));
        emitter.onError(e -> sseService.remove(sessionId));
        sseService.register(sessionId, emitter);
        
        String first = null;
        NextQuestionDto firstQuestion = null;
        if (useChapterMode) {
            // 새로운 챕터 기반 모드 사용
            try {
                firstQuestion = chapterBasedService.initializeSession(sessionId);
                first = firstQuestion.getQuestionText();
            } catch (Exception e) {
                // 챕터 기반 서비스 실패시 레거시 모드로 fallback
                first = convService.getNextQuestion(sessionId);
            }
        } else {
            // 레거시 모드
            first = convService.getNextQuestion(sessionId);
        }
        
        if (first != null) {
            // DB 저장(QUESTION 메시지)도 여기서 해주면 추적이 명확해집니다.
            convService.createMessage(
                    ConversationMessageRequest.builder()
                            .sessionId(sessionId)
                            .messageType(MessageType.QUESTION)
                            .content(first)
                            .build()
            );
            
            // QuestionResponse 생성 (챕터 정보 포함)
            QuestionResponse.QuestionResponseBuilder responseBuilder = QuestionResponse.builder()
                    .text(first);
            
            if (firstQuestion != null) {
                responseBuilder
                        .currentChapter(firstQuestion.getCurrentChapterName())
                        .currentStage(firstQuestion.getCurrentStageName())
                        .questionType(firstQuestion.getQuestionType())
                        .overallProgress(firstQuestion.getOverallProgress())
                        .chapterProgress(firstQuestion.getChapterProgress())
                        .isLastQuestion(firstQuestion.isLastQuestion());
            }
            
            sseService.pushQuestion(sessionId, responseBuilder.build());
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
    @PostMapping("/next")
    public ResponseEntity<Void> nextQuestion(@RequestParam String sessionId) {
        // 1) 서비스에서 다음 질문 꺼내기 - 챕터 기반 모드 우선 시도
        ConversationSession session = convService.getSessionEntity(sessionId);
        NextQuestionDto nextQuestionDto = null;
        String next = null;
        
        if (session != null && session.getCurrentChapterId() != null) {
            // 챕터 기반 모드
            try {
                String lastAnswer = convService.getLastAnswer(sessionId);
                nextQuestionDto = chapterBasedService.getNextQuestion(sessionId, lastAnswer);
                next = nextQuestionDto.getQuestionText();
            } catch (Exception e) {
                // 오류시 레거시 모드로 fallback
                next = convService.getNextQuestion(sessionId);
            }
        } else {
            // 레거시 모드
            next = convService.getNextQuestion(sessionId);
        }
        
        if (next != null) {
            // 2) DB에 QUESTION 메시지 저장
            convService.createMessage(
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
}
