package com.c203.autobiography.domain.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "대화 세션에 대한 첫 질문 요청 DTO")
public class QuestionRequest {

    @NotBlank(message = "sessionId는 필수입니다.")
    @Schema(description = "대화 세션 고유 식별자", example = "session-uuid-1234")
    private String sessionId;

    @NotNull(message = "episodeId는 필수입니다.")
    @Schema(description = "연결할 에피소드 ID", example = "1001")
    private Long episodeId;
}
