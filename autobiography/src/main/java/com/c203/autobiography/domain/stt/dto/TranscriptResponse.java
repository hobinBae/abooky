package com.c203.autobiography.domain.stt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "서버가 클라이언트로 보내는 STT 결과 DTO")
public class TranscriptResponse {

    @Schema(description = "청크 순서 인덱스", example = "0")
    private Integer chunkIndex;

    @Schema(description = "인식된 텍스트 부분", example = "저는 1990년 5월 17일에 태어났습니다.")
    private String text;
}
