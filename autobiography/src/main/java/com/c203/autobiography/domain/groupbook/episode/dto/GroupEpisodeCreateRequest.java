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

    @NotBlank
    private String template;

    private Integer orderNo;

    // 🎯 첫 답변 관련 필드 추가
    @NotBlank
    private String firstAnswer;      // 사용자의 첫 답변

    private String questionKey;      // 답변하는 질문의 키 (검증용)

}
