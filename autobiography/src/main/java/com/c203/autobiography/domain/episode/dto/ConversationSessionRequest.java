package com.c203.autobiography.domain.episode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "대화 세션 생성 요청")
public class ConversationSessionRequest {
    @NotBlank
    @Schema(description = "세션 ID (UUID)", example = "session-uuid-1234")
    private String sessionId;

    @Schema(description = "연결할 에피소드 ID", example = "1001")
    private Long episodeId;

}
