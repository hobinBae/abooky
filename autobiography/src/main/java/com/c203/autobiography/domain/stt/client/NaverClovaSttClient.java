package com.c203.autobiography.domain.stt.client;

import com.c203.autobiography.domain.stt.dto.SttResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;



@Slf4j
@Component
@RequiredArgsConstructor
public class NaverClovaSttClient implements SttClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${clova.stt.url}")
    private String apiUrl;

    @Value("${clova.stt.api-key}")
    private String apiKey;

    @Override
    public SttResponse recognize(
            MultipartFile audio,
            String lang,
            boolean assessment,
            String utterance,
            String boostings,
            boolean graph
    ) {
        try {
            // 오디오 파일 유효성 검사
            validateAudioFile(audio);
            log.info("서버에서 받은 MultipartFile 크기: {} bytes", audio.getSize());
            // URL 구성 (개선된 파라미터 처리)
            String url = buildApiUrl(lang, assessment, utterance, boostings, graph);
            log.info("Clova STT API 호출: {}, 파일크기: {}bytes", url, audio.getSize());

            // 헤더 설정
            HttpHeaders headers = createHeaders();

            // 요청 생성 및 전송
            HttpEntity<byte[]> request = new HttpEntity<>(audio.getBytes(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            // 응답 처리
            return parseResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("Clova STT 클라이언트 오류: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("STT 요청 오류: " + e.getStatusCode() + " - " + getErrorMessage(e.getResponseBodyAsString()));
        } catch (HttpServerErrorException e) {
            log.error("Clova STT 서버 오류: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("STT 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        } catch (Exception e) {
            log.error("Clova STT 호출 중 예외 발생", e);
            throw new RuntimeException("STT 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void validateAudioFile(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("오디오 파일이 비어있습니다.");
        }

        // 파일 크기 검사 (최대 60초, 약 10MB)
        if (audio.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("오디오 파일이 너무 큽니다. (최대 10MB)");
        }

        // 최소 크기 검사
        if (audio.getSize() < 1000) {
            throw new IllegalArgumentException("오디오 파일이 너무 작습니다. 최소 1초 이상 녹음해주세요.");
        }

        // 파일 형식 검사
        String contentType = audio.getContentType();
        if (contentType != null && !isValidAudioType(contentType)) {
            log.warn("지원되지 않는 오디오 형식: {}", contentType);
        }
    }

    private boolean isValidAudioType(String contentType) {
        return contentType.startsWith("audio/") ||
                contentType.equals("application/octet-stream");
    }

    private String buildApiUrl(String lang, boolean assessment, String utterance, String boostings, boolean graph) {
        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?lang=").append(URLEncoder.encode(lang, StandardCharsets.UTF_8));

        if (assessment) {
            urlBuilder.append("&assessment=true");
        }

        if (StringUtils.hasText(utterance)) {
            urlBuilder.append("&utterance=").append(URLEncoder.encode(utterance, StandardCharsets.UTF_8));
        }

        // boostings 파라미터 개선 (한국어 특화)
        if (StringUtils.hasText(boostings)) {
            urlBuilder.append("&boostings=").append(URLEncoder.encode(boostings, StandardCharsets.UTF_8));
        } else if ("Kor".equals(lang)) {
            // 한국어의 경우 기본 부스팅 단어 추가
            String defaultBoostings = "안녕하세요\t감사합니다\t죄송합니다";
            urlBuilder.append("&boostings=").append(URLEncoder.encode(defaultBoostings, StandardCharsets.UTF_8));
        }

        if (graph) {
            urlBuilder.append("&graph=true");
        }

        return urlBuilder.toString();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("X-CLOVASPEECH-API-KEY", apiKey);
        headers.set("Accept", "application/json");
        return headers;
    }

    private SttResponse parseResponse(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);

            // 에러 응답 확인
            if (root.has("errorCode")) {
                String errorCode = root.path("errorCode").asText();
                String errorMessage = root.path("errorMessage").asText();
                log.error("Clova STT API 에러: {} - {}", errorCode, errorMessage);
                throw new RuntimeException("STT 인식 실패: " + errorMessage);
            }

            // 정상 응답 처리
            String text = root.path("text").asText();

            if (!StringUtils.hasText(text)) {
                log.warn("STT 응답에서 텍스트를 찾을 수 없음: {}", responseBody);
                return new SttResponse("음성을 인식할 수 없습니다.");
            }

//            // 텍스트 후처리
//            text = cleanupText(text);
//            log.info("STT 인식 결과: {}", text);

            return new SttResponse(text);

        } catch (Exception e) {
            log.error("STT 응답 파싱 실패: {}", responseBody, e);
            throw new RuntimeException("STT 응답 처리 중 오류가 발생했습니다.");
        }
    }

    private String cleanupText(String text) {
        if (text == null) return "";

        // 불필요한 공백 제거 및 정리
        return text.trim()
                .replaceAll("\\s+", " ")  // 여러 공백을 하나로
                .replaceAll("^[\\s.]+", "")  // 시작 부분 공백/점 제거
                .replaceAll("[\\s.]+$", "");  // 끝 부분 공백/점 제거
    }

    private String getErrorMessage(String errorResponse) {
        try {
            JsonNode errorNode = mapper.readTree(errorResponse);
            return errorNode.path("errorMessage").asText("알 수 없는 오류");
        } catch (Exception e) {
            return "응답 파싱 실패";
        }
    }
}