package com.c203.autobiography.domain.episode.conversation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "대화 메시지 생성 요청 DTO")
public class ConversationMessageRequest {
    @NotBlank
    @Schema(description = "세션 ID (UUID)", example = "session-uuid-1234")
    private String sessionId;

    @NotBlank
    @Schema(description = "메시지 타입 (QUESTION, PARTIAL, FINAL, EPISODE)", example = "PARTIAL")
    private MessageType messageType;

    @Schema(description = "청크 인덱스 (PARTIAL 메시지 시)", example = "0")
    private Integer chunkIndex;

    @NotBlank
    @Schema(description = "메시지 내용", example = "저는 1990년...살았어요.")
    private String content;

}
