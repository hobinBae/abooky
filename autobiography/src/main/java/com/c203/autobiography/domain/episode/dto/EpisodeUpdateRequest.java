package com.c203.autobiography.domain.episode.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "에피소드 수정 요청 dto")
public class EpisodeUpdateRequest {

    @Size(max = 100, message = "제목은 최대 100자까지 가능합니다.")
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate episodeDate;

    private Integer episodeOrder;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @Size(max = 255, message = "오디오 URL은 최대 255자까지 가능합니다.")
    private String audioUrl;

}
