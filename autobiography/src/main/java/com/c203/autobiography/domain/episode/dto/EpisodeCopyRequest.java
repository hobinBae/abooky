package com.c203.autobiography.domain.episode.dto;

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
public class EpisodeCopyRequest {
    @NotNull
    private Long episodeId;

    @Builder.Default
    private boolean delete = false;

    private String title;
    private String content;
    private LocalDate episodeDate;
    private Integer episodeOrder;
    private String audioUrl;
}
