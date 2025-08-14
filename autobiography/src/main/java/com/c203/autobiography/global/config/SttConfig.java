package com.c203.autobiography.global.config;

import com.c203.autobiography.domain.stt.client.LiveKitSttClient;
import com.c203.autobiography.domain.stt.client.SttClient;
import com.c203.autobiography.domain.stt.client.WhisperSttClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SttConfig {

    @Value("${stt.provider:whisper}")
    private String sttProvider;

    @Bean
    public SttClient sttClient(WhisperSttClient whisperSttClient, LiveKitSttClient liveKitSttClient) {
        log.info("🎤 STT Provider 설정: {}", sttProvider);
        
        switch (sttProvider.toLowerCase()) {
            case "livekit":
            case "deepgram":
                log.info("✅ LiveKit + Deepgram STT 클라이언트 사용");
                return liveKitSttClient;
            case "whisper":
            default:
                log.info("✅ Whisper STT 클라이언트 사용");
                return whisperSttClient;
        }
    }
}