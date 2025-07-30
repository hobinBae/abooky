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
@Schema(description = "대화 메시지 수정 요청 DTO")
public class ConversationMessageUpdateRequest {
    @NotNull
    @Schema(description = "메시지 ID", example = "2002")
    private Long messageId;

    @NotBlank
    @Schema(description = "수정할 메시지 내용", example = "새로운 내용")
    private String content;
}
