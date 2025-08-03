package com.c203.autobiography.domain.episode.dto;

import com.c203.autobiography.domain.episode.entity.ConversationSession;
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
@Schema(description = "대화 세션 응답 DTO")
public class ConversationSessionResponse {
    @Schema(description = "세션 ID", example = "session-uuid-1234")
    private String sessionId;

    @Schema(description = "책 ID", example = "5001")
    private Long bookId;

    @Schema(description = "에피소드 ID", example = "1001")
    private Long episodeId;

    @Schema(description = "템플릿 질문 인덱스", example = "0")
    private Integer templateIndex;

    @Schema(description = "세션 상태", example = "OPEN")
    private SessionStatus status;

    @Schema(description = "누적 토큰 수", example = "120")
    private Long tokenCount;

    @Schema(description = "마지막 메시지 시각")
    private LocalDateTime lastMessageAt;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    public static ConversationSessionResponse from(ConversationSession c){
        return ConversationSessionResponse.builder()
                .sessionId(c.getSessionId())
                .bookId(c.getBookId())
                .episodeId(c.getEpisodeId())
                .templateIndex(c.getTemplateIndex())
                .status(c.getStatus())
                .tokenCount(c.getTokenCount())
                .lastMessageAt(c.getLastMessageAt())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
