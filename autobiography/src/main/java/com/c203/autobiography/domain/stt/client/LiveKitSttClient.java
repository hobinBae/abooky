package com.c203.autobiography.domain.stt.client;

import com.c203.autobiography.domain.stt.dto.SttResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveKitSttClient implements SttClient {

    @Value("${deepgram.api-key:}")
    private String deepgramApiKey;

    private RestTemplate restTemplate;

    @PostConstruct
    public void initRestTemplate() {
        this.restTemplate = new RestTemplate();
        log.info("LiveKit + Deepgram STT Client 초기화 완료");
    }

    private static final String DEEPGRAM_URL = "https://api.deepgram.com/v1/listen";

    @Override
    public SttResponse recognize(MultipartFile audio) {
        return recognize(audio, null);
    }

    @Override
    public SttResponse recognize(MultipartFile audio, String customProperNouns) {
        try {
            log.info("🚀 Deepgram STT 요청 시작: filename={}, size={} bytes", 
                     audio.getOriginalFilename(), audio.getSize());

            // Deepgram API 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + deepgramApiKey);
            headers.set("Content-Type", audio.getContentType() != null ? audio.getContentType() : "audio/webm");

            // 오디오 데이터를 직접 전송
            HttpEntity<byte[]> request = new HttpEntity<>(audio.getBytes(), headers);

            // Deepgram 쿼리 파라미터로 한국어 최적화 설정
            String urlWithParams = DEEPGRAM_URL + 
                "?model=enhanced" +           // Enhanced 모델 사용
                "&language=ko" +              // 한국어 설정
                "&punctuate=true" +           // 구두점 추가
                "&diarize=false" +            // 화자 분리 비활성화
                "&smart_format=true" +        // 스마트 포맷팅
                "&profanity_filter=false" +   // 욕설 필터 비활성화
                "&redact=false";              // 개인정보 마스킹 비활성화

            log.info("🎯 Deepgram 요청 URL: {}", urlWithParams);

            // Deepgram API 호출
            ResponseEntity<DeepgramResponse> response = restTemplate.postForEntity(
                urlWithParams, request, DeepgramResponse.class);

            DeepgramResponse deepgramResp = response.getBody();
            
            if (deepgramResp != null && deepgramResp.getResults() != null && 
                !deepgramResp.getResults().getChannels().isEmpty()) {
                
                String transcript = deepgramResp.getResults().getChannels().get(0)
                    .getAlternatives().get(0).getTranscript();
                
                log.info("✅ Deepgram STT 결과: text='{}'", transcript);
                
                return SttResponse.builder()
                    .text(transcript)
                    .build();
            } else {
                log.warn("⚠️ Deepgram 응답이 비어있습니다");
                return SttResponse.builder()
                    .text("")
                    .build();
            }

        } catch (IOException e) {
            log.error("Deepgram STT 처리 중 IO 오류", e);
            throw new RuntimeException("Deepgram STT 처리 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Deepgram STT 처리 중 예외 발생", e);
            throw new RuntimeException("Deepgram STT 처리 실패: " + e.getMessage(), e);
        }
    }

    // Deepgram API 응답 모델
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepgramResponse {
        private DeepgramResults results;

        public DeepgramResults getResults() { return results; }
        public void setResults(DeepgramResults results) { this.results = results; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepgramResults {
        private List<DeepgramChannel> channels;

        public List<DeepgramChannel> getChannels() { return channels; }
        public void setChannels(List<DeepgramChannel> channels) { this.channels = channels; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepgramChannel {
        private List<DeepgramAlternative> alternatives;

        public List<DeepgramAlternative> getAlternatives() { return alternatives; }
        public void setAlternatives(List<DeepgramAlternative> alternatives) { this.alternatives = alternatives; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepgramAlternative {
        private String transcript;
        private double confidence;

        public String getTranscript() { return transcript; }
        public void setTranscript(String transcript) { this.transcript = transcript; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
}