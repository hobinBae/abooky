package com.c203.autobiography.domain.episode.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "에피소드 제목", example = "유년기의 추억")
    private String title;

    @NotNull(message = "episodeDate는 필수입니다.")
    @Schema(description = "에피소드 날짜", example = "1990-05-17")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate episodeDate;

    @Schema(description = "에피소드 순서", example = "1")
    private Integer episodeOrder;

    @NotBlank(message = "content는 필수입니다.")
    @Schema(description = "에피소드 본문 내용", example = "광주에서 태어난 나는 ...")
    private String content;

    @Schema(description = "저장된 오디오 URL", example = "https://.../audio.webm")
    private String audioUrl;
    /**
     * Service/Controller 레이어에서 사용하기 편하도록
     * 엔티티 변환 메서드를 추가할 수도 있습니다.
     */
    /*public Episode toEntity(Book book) {
        return Episode.builder()
            .book(book)
            .title(title)
            .episodeDate(episodeDate)
            .episodeOrder(episodeOrder)
            .content(content)
            .audioUrl(audioUrl)
            .build();
    }*/

}
