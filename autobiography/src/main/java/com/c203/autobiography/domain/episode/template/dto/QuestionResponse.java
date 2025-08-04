package com.c203.autobiography.domain.episode.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI가 생성한 질문 응답 DTO")
public class QuestionResponse {

    @Schema(description = "AI 질문 텍스트", example = "당신의 출생은 언제입니까?")
    private String text;
}
