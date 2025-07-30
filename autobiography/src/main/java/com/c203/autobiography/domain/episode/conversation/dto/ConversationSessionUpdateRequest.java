package com.c203.autobiography.domain.episode.conversation.dto;

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
@Schema(description = "대화 세션 업데이트 요청")
public class ConversationSessionUpdateRequest {

    @NotBlank
    @Schema(description = "세션 ID (UUID)", example = "session-uuid-1234")
    private String sessionId;

    @NotNull
    @Schema(description = "새 상태", example = "CLOSED")
    private SessionStatus status;

    @NotNull
    @Schema(description = "템플릿 인덱스 업데이트 값", example = "3")
    private Integer templateIndex;


}
