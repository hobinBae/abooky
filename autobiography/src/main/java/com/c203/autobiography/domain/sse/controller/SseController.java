//package com.c203.autobiography.domain.sse.controller;
//
//import static java.util.Arrays.stream;
//
//import com.c203.autobiography.domain.episode.dto.ConversationSessionRequest;
//import com.c203.autobiography.domain.episode.service.ConversationService;
//import com.c203.autobiography.domain.sse.service.SseService;
//import com.c203.autobiography.domain.episode.template.dto.QuestionRequest;
//import com.c203.autobiography.domain.episode.template.dto.QuestionResponse;
//import com.c203.autobiography.domain.episode.template.service.QuestionTemplateService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/stream/questions")
//public class SseController {
//
//    private final SseService sseService;
//    private final QuestionTemplateService templateService;
//    private final ConversationService conversationService;
//
//    /**
//     * SSE 스트림 연결 및 첫 질문 푸시
//     * @param request 세션 및 에피소드 정보를 담은 DTO
//     */
//    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter streamQuestions(@Valid QuestionRequest request) {
//        String sessionId = request.getSessionId();
//        Long episodeId = request.getEpisodeId();
//
//        // 1) 대화 세션 생성
//        conversationService.createSession(
//                new ConversationSessionRequest(sessionId, null)
//        );
//
//        // 2) SseEmitter 생성 및 이벤트 핸들러 등록
//        SseEmitter emitter = new SseEmitter(0L);
//        emitter.onTimeout(() -> sseService.remove(sessionId));
//        emitter.onCompletion(() -> sseService.remove(sessionId));
//        emitter.onError(ex -> sseService.remove(sessionId));
//
//        // 3) 세션에 Emitter 등록
//        sseService.register(sessionId, emitter);
//
//        // 4) 첫 질문 생성 및 푸시
//        String firstText = templateService.getByOrder(0);
//        QuestionResponse question = QuestionResponse.builder()
//                .text(firstText)
//                .build();
//        sseService.pushQuestion(sessionId, question);
//
//        return emitter;
//    }
//}
