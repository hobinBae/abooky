package com.c203.autobiography.domain.stt.controller;

import com.c203.autobiography.domain.episode.conversation.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.conversation.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.conversation.dto.MessageType;
import com.c203.autobiography.domain.episode.conversation.service.ConversationService;
import com.c203.autobiography.domain.sse.service.SseService;
import com.c203.autobiography.domain.stt.client.SttClient;
import com.c203.autobiography.domain.stt.dto.SttRequest;
import com.c203.autobiography.domain.stt.dto.SttResponse;
import com.c203.autobiography.domain.stt.dto.TranscriptRequest;
import com.c203.autobiography.domain.stt.dto.TranscriptResponse;
import com.c203.autobiography.domain.stt.service.SttService;
import com.c203.autobiography.domain.stt.service.SttServiceImpl;
import com.c203.autobiography.domain.template.dto.QuestionResponse;
import com.c203.autobiography.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stt")
@CrossOrigin(origins = "*")
public class SttController {
    private final SttService sttService;
    private final SseService sseService;
    private final ConversationService conversationService;

//    @PostMapping(value = "/whisper", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public SttResponse recognizeWithWhisper(@RequestPart("audio") MultipartFile audio,
//                                            @RequestParam(required = false) String customProperNouns) {
//        SttRequest req = SttRequest.builder()
//                .audio(audio)
//                // customProperNouns는 optional, null 허용
//                .build();
//        // 서비스 메서드 호출: customProperNouns 전달
//        return sttService.recognize(audio, customProperNouns);
//    }

    @PostMapping(value = "/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> processChunk(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("chunkIndex") @Min(0) int chunkIndex,
            @RequestPart("audio") MultipartFile audio,
            @RequestParam(value = "customProperNouns", required = false) String customProperNouns,
            @RequestParam(value = "finalTranscript", defaultValue = "false") boolean finalTranscript,
            HttpServletRequest httpRequest

    ) {
        // 1) Whisper STT 호출
        SttResponse sttResp = sttService.recognize(audio, customProperNouns);

        // 2) 대화 메시지 저장 (PARTIAL)
        conversationService.createMessage(
                ConversationMessageRequest.builder()
                        .sessionId(sessionId)
                        .messageType(MessageType.PARTIAL)
                        .chunkIndex(chunkIndex)
                        .content(sttResp.getText())
                        .build()
        );
        // 3) SSE로 부분 인식 결과 푸시
        TranscriptResponse partialDto = TranscriptResponse.builder()
                .chunkIndex(chunkIndex)
                .text(sttResp.getText())
                .build();
        sseService.pushPartialTranscript(sessionId, partialDto);

        // 4) 최종 청크일 경우
        if (finalTranscript) {
            // 메시지 저장 (FINAL)
            conversationService.createMessage(
                    ConversationMessageRequest.builder()
                            .sessionId(sessionId)
                            .messageType(MessageType.FINAL)
                            .chunkIndex(chunkIndex)
                            .content(sttResp.getText())
                            .build()
            );

            // SSE로 최종 인식 결과 푸시
            List<ConversationMessageResponse> history = conversationService.getHistory(sessionId);
            String fullText = history.stream()
                    .map(ConversationMessageResponse::getContent)
                    .collect(Collectors.joining(" "));

            // SSE로 최종 인식 결과 푸시
            TranscriptResponse finalDto = TranscriptResponse.builder()
                    .chunkIndex(chunkIndex)
                    .text(fullText)
                    .build();
            sseService.pushFinalTranscript(sessionId, finalDto);

            // 다음 질문 생성 및 푸시
            String next = conversationService.getNextQuestion(sessionId);
            QuestionResponse question = QuestionResponse.builder()
                    .text(next)
                    .build();
            sseService.pushQuestion(sessionId, question);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.of(HttpStatus.CREATED, "성공", null, httpRequest.getRequestURI()));
    }

}
