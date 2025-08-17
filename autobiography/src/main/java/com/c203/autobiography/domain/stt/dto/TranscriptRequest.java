package com.c203.autobiography.domain.stt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "클라이언트가 전송하는 STT 결과 요청 DTO")
public class TranscriptRequest {
    @NotBlank(message = "sessionId는 필수입니다.")
    @Schema(description = "대화 세션 고유 식별자", example = "session-uuid-1234")
    private String sessionId;

    @NotNull(message = "chunkIndex는 필수입니다.")
    @Min(value = 0, message = "chunkIndex는 0 이상이어야 합니다.")
    @Schema(description = "청크 순서 인덱스", example = "0")
    private Integer chunkIndex;

    @NotBlank(message = "text는 필수입니다.")
    @Schema(description = "STT로 변환된 텍스트", example = "저는 1990년 5월 17일에 태어났습니다.")
    private String text;

    @Schema(description = "최종 청크 여부", example = "false")
    private boolean finalTranscript;
}
