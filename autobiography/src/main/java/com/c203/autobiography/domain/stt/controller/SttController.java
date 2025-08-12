package com.c203.autobiography.domain.stt.controller;

import com.c203.autobiography.domain.episode.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.dto.MessageType;
import com.c203.autobiography.domain.episode.entity.ConversationMessage;
import com.c203.autobiography.domain.episode.service.ConversationService;
import com.c203.autobiography.domain.sse.service.SseService;
import com.c203.autobiography.domain.stt.dto.SttResponse;
import com.c203.autobiography.domain.stt.dto.TranscriptResponse;
import com.c203.autobiography.domain.stt.service.SttService;
import com.c203.autobiography.domain.episode.template.dto.QuestionResponse;
import com.c203.autobiography.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "STT API", description = "STT 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stt")
@CrossOrigin(origins = "*")
@Slf4j
public class SttController {
    private final SttService sttService;
    private final SseService sseService;
    private final ConversationService conversationService;


    @PostMapping(value = "/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> processChunk(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("chunkIndex") @Min(0) int chunkIndex,
            @RequestPart("audio") MultipartFile audio,
            @RequestParam(value = "customProperNouns", required = false) String customProperNouns,
            HttpServletRequest httpRequest

    ) {
        // 1) Whisper STT 호출
        SttResponse sttResp = sttService.recognize(audio, customProperNouns);
        log.info(sttResp.getText() + "stt");
        // 2) 대화 메시지 저장 (PARTIAL)
         ConversationMessageResponse conversation =  conversationService.createMessage(
                ConversationMessageRequest.builder()
                        .sessionId(sessionId)
                        .messageType(MessageType.ANSWER)
                        .chunkIndex(chunkIndex)
                        .content(sttResp.getText())
                        .build()
        );
        // 3) SSE로 부분 인식 결과 푸시
        TranscriptResponse partialDto = TranscriptResponse.builder()
                .messageId(conversation.getMessageId())
                .chunkIndex(chunkIndex)
                .text(sttResp.getText())
                .build();

        sseService.pushPartialTranscript(sessionId, partialDto);

//            // 3) 다음 질문 준비 완료 알림만
//            sseService.pushQuestion(
//                    sessionId,
//                    QuestionResponse.builder()
//                            .text("🔔 다음 질문이 준비되었습니다. 버튼을 눌러주세요.")
//                            .build()
//            );


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "성공", null, httpRequest.getRequestURI()));
    }

}