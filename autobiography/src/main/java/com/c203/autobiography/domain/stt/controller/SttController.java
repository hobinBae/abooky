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

    ){
        // 비즈니스 로직은 서비스로 위임
        String transcribedText = sttService.processAudioChunk(sessionId, chunkIndex, audio, customProperNouns);

        // 결과 텍스트를 응답 Body에도 담아줌 (SSE를 놓쳤을 경우 대비 or 디버깅용)
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "음성 인식 성공", null, httpRequest.getRequestURI()));
    }

}