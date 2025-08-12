package com.c203.autobiography.domain.groupbook.episode.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupEpisodeUpdateRequest {
    @NotBlank private String template;
    @NotBlank
    private String title;
    private Integer orderNo;
    private String editedContent;
}
