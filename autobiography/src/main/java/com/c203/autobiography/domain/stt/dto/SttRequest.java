package com.c203.autobiography.domain.stt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SttRequest {
    /**
     * 업로드된 오디오 파일(PCM WAV, WebM 등)
     */
    private MultipartFile audio;

    /**
     * Whisper용 고유명사 프롬프트 문자열
     */
    private String customProperNouns;
}
