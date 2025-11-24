package com.c203.autobiography.domain.stt.service;

import com.c203.autobiography.domain.sse.service.SseService;
import com.c203.autobiography.domain.stt.client.SttClient;
import com.c203.autobiography.domain.stt.dto.SttRequest;
import com.c203.autobiography.domain.stt.dto.SttResponse;
import com.c203.autobiography.domain.stt.dto.TranscriptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class SttServiceImpl implements SttService {
    private final SttClient sttClient;
    private final SseService sseService;
    @Override
    public SttResponse recognize(MultipartFile audio) {
        return sttClient.recognize(audio);
    }

    @Override
    public SttResponse recognize(MultipartFile audio, String customProperNouns) {
        return sttClient.recognize(audio, customProperNouns);
    }

    @Override
    public void processAudioChunk(String sessionId, int chunkIndex, MultipartFile audio, String customProperNouns) {
        log.info("🎙️ STT 서비스 처리 시작: sessionId={}, chunkIndex={}", sessionId, chunkIndex);

        // 1. STT 변환 (Deepgram 호출)
        SttResponse sttResp = sttClient.recognize(audio, customProperNouns);
        String transcribedText = sttResp.getText();

        log.info("🗣️ 변환된 텍스트: '{}'", transcribedText);

        // 2. SSE로 클라이언트에 푸시
        // DB에 저장하지 않았으므로 messageId는 null로 보냅니다.
        // 클라이언트는 이 텍스트를 받아서 입력창에 보여주기만 하면 됩니다.
        TranscriptResponse partialDto = TranscriptResponse.builder()
                .messageId(null) // 아직 저장 안 됨
                .chunkIndex(chunkIndex)
                .text(transcribedText)
                .build();

        sseService.pushPartialTranscript(sessionId, partialDto);
        log.info("📡 SSE 전송 완료 (DB 저장 건너뜀)");

    }
}
