package com.c203.autobiography.domain.episode.conversation.dto;

import com.c203.autobiography.domain.episode.conversation.entity.ConversationMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "대화 메시지 응답 DTO")
public class ConversationMessageResponse {
    @Schema(description = "메시지 ID", example = "2002")
    private Long messageId;

    @Schema(description = "세션 ID (UUID)", example = "session-uuid-1234")
    private String sessionId;

    @Schema(description = "메시지 타입", example = "PARTIAL")
    private MessageType messageType;

    @Schema(description = "청크 인덱스", example = "0")
    private Integer chunkIndex;

    @Schema(description = "메시지 내용")
    private String content;

    @Schema(description = "메시지 순서", example = "3")
    private Integer messageNo;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    public static ConversationMessageResponse from(ConversationMessage c){
        return ConversationMessageResponse.builder()
                .messageId(c.getMessageId())
                .sessionId(c.getSessionId())
                .messageType(c.getMessageType())
                .chunkIndex(c.getChunkIndex())
                .content(c.getContent())
                .messageNo(c.getMessageNo())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();

    }
}
