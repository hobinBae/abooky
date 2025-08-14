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
import org.springframework.boot.web.client.RestTemplateBuilder;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveKitSttClient implements SttClient {

    @Value("${deepgram.api-key:}")
    private String deepgramApiKey;

    private RestTemplate restTemplate;

    @PostConstruct
    public void initRestTemplate() {
        // 긴 음성 파일 처리를 위한 타임아웃 설정
        this.restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(10))    // 연결 타임아웃: 10초
                .setReadTimeout(Duration.ofSeconds(120))      // 읽기 타임아웃: 120초 (긴 음성 처리용)
                .build();
        log.info("LiveKit + Deepgram STT Client 초기화 완료 (타임아웃: 연결 10초, 읽기 120초)");
    }

    private static final String DEEPGRAM_URL = "https://api.deepgram.com/v1/listen";

    @Override
    public SttResponse recognize(MultipartFile audio) {
        return recognize(audio, null);
    }

    @Override
    public SttResponse recognize(MultipartFile audio, String customProperNouns) {
        try {
            log.info("🚀 Deepgram STT 요청 시작: filename={}, size={} bytes, contentType={}", 
                     audio.getOriginalFilename(), audio.getSize(), audio.getContentType());

            // API 키 검증
            if (deepgramApiKey == null || deepgramApiKey.trim().isEmpty()) {
                log.error("❌ Deepgram API 키가 설정되지 않았습니다");
                throw new RuntimeException("Deepgram API 키가 설정되지 않았습니다");
            }

            // 파일 크기 검증
            if (audio.isEmpty()) {
                log.error("❌ 오디오 파일이 비어있습니다");
                throw new RuntimeException("오디오 파일이 비어있습니다");
            }

            // Deepgram API 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + deepgramApiKey);
            headers.set("Content-Type", audio.getContentType() != null ? audio.getContentType() : "audio/webm");

            // 오디오 데이터를 직접 전송
            byte[] audioBytes = audio.getBytes();
            log.info("📁 오디오 바이트 배열 크기: {} bytes", audioBytes.length);
            
            HttpEntity<byte[]> request = new HttpEntity<>(audioBytes, headers);

            // Deepgram 쿼리 파라미터로 한국어 띄어쓰기 최적화 설정
            String urlWithParams = DEEPGRAM_URL + 
                "?model=nova-2" +             // Nova-2 모델 (한국어 성능 개선)
                "&language=ko" +              // 한국어 설정
                "&punctuate=true" +           // 구두점 추가
                "&diarize=false" +            // 화자 분리 비활성화
                "&smart_format=true" +        // 스마트 포맷팅 (띄어쓰기 개선)
                "&profanity_filter=false" +   // 욕설 필터 비활성화
                "&redact=false" +             // 개인정보 마스킹 비활성화
                "&filler_words=false" +       // 불필요한 단어 제거
                "&numerals=true" +            // 숫자를 아라비아 숫자로 변환
                "&utterances=true" +          // 문장 단위 분리
                "&paragraphs=true";           // 문단 단위 분리

            log.info("🎯 Deepgram 요청 URL: {}", urlWithParams);
            log.info("🔑 API 키 앞 4자리: {}****", deepgramApiKey.substring(0, 4));

            // Deepgram API 호출
            ResponseEntity<DeepgramResponse> response = restTemplate.postForEntity(
                urlWithParams, request, DeepgramResponse.class);

            log.info("📡 Deepgram 응답 상태: {}", response.getStatusCode());

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
                log.warn("⚠️ Deepgram 응답이 비어있습니다: {}", deepgramResp);
                return SttResponse.builder()
                    .text("")
                    .build();
            }

        } catch (IOException e) {
            log.error("❌ Deepgram STT 처리 중 IO 오류: {}", e.getMessage(), e);
            throw new RuntimeException("Deepgram STT 처리 실패 (IO 오류): " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Deepgram STT 처리 중 예외 발생: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
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