package com.c203.autobiography.domain.episode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "에피소드 복제 시 사용되는 요청 DTO")
public class EpisodeCopyRequest {
    @NotNull
    @Schema(description = "원본 에피소드 ID", example = "5")
    private Long episodeId;

    @Schema(description = "삭제 여부 (true이면 복제하지 않음)", example = "false")
    @Builder.Default
    private boolean delete = false;

    @Size(max = 100, message = "제목은 최대 100자까지 가능합니다.")
    @Schema(description = "새로운 에피소드 제목 (선택)", example = "추억의 2막")
    private String title;

    @Schema(description = "새로운 에피소드 본문 (선택)", example = "이 부분만 변경해서 보여줍니다.")
    private String content;

    @Schema(description = "새로운 에피소드 날짜 (선택)", example = "2025-07-21")
    private LocalDate episodeDate;

    @Schema(description = "새로운 에피소드 순서 (선택)", example = "2")
    private Integer episodeOrder;

    @Schema(description = "새로운 오디오 URL (선택)", example = "https://.../audio.webm")
    private String audioUrl;
}
