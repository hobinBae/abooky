package com.c203.autobiography.domain.episode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "에피소드 응답 DTO")
public class EpisodeResponse {
    @Schema(description = "에피소드 ID", example = "1001")
    private Long episodeId;

    @Schema(description = "책 ID (Book)", example = "5001")
    private Long bookId;

    @Schema(description = "에피소드 제목", example = "유년기의 추억")
    private String title;

    @Schema(description = "에피소드 날짜", example = "1990-05-17")
    private LocalDate episodeDate;

    @Schema(description = "에피소드 순서", example = "1")
    private Integer episodeOrder;

    @Schema(description = "에피소드 본문 내용", example = "광주에서 태어난 나는 ...")
    private String content;

    @Schema(description = "저장된 오디오 URL", example = "https://.../audio.webm")
    private String audioUrl;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
}
