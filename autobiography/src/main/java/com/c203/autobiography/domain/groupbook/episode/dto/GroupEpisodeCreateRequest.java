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
public class GroupEpisodeCreateRequest {
    @NotBlank
    private String title;

    private String summary;          // 에피소드 요약 (선택)

    private Integer orderNo;         // 순서 (선택, 없으면 자동 설정)
    
    private String template;         // 템플릿 (선택, 없으면 INTRO)

}
