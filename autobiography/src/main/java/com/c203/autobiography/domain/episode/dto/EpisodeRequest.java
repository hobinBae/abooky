package com.c203.autobiography.domain.episode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "에피소드 저장 요청 DTO")
public class EpisodeRequest {

    @NotNull(message = "bookId는 필수입니다.")
    @Schema(description = "책 ID (Book)", example = "5001")
    private Long bookId;

    @NotBlank(message = "title은 필수입니다.")
    @Schema(description = "에피소드 제목", example = "유년기의 추억")
    private String title;

    @NotNull(message = "episodeDate는 필수입니다.")
    @Schema(description = "에피소드 날짜", example = "1990-05-17")
    private LocalDate episodeDate;

    @NotNull(message = "episodeOrder는 필수입니다.")
    @Schema(description = "에피소드 순서", example = "1")
    private Integer episodeOrder;

    @NotBlank(message = "content는 필수입니다.")
    @Schema(description = "에피소드 본문 내용", example = "광주에서 태어난 나는 ...")
    private String content;

    @Schema(description = "저장된 오디오 URL", example = "https://.../audio.webm")
    private String audioUrl;

}
