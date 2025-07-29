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
    private MultipartFile audio;       // 오디오 바이트
    private String lang;               // Kor, Eng, Jpn, Chn
    private Boolean assessment = false;// 발음평가 여부
    private String utterance;          // 평가対象 텍스트 (optional)
    private String boostings;          // 탭(\t) 구분 키워드 (optional)
    private Boolean graph = false;     // 파형 그래프 반환 여부
}
