package com.c203.autobiography.domain.groupbook.episode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹 에피소드 답변 교정 요청 DTO")
public class GroupAnswerCorrectionRequest {

    @NotBlank(message = "질문은 필수입니다.")
    @Schema(description = "AI가 제시한 질문", example = "어린 시절 가장 기억에 남는 가족 여행은 무엇인가요?")
    private String question;

    @NotBlank(message = "답변은 필수입니다.")
    @Schema(description = "사용자가 작성한 답변", example = "5살 때 부모님과 함께 간 제주도 여행이 가장 기억에 남아요...")
    private String answer;

    @Schema(description = "현재 템플릿", example = "INTRO")
    private String currentTemplate;

    @Schema(description = "그룹 타입", example = "FAMILY")
    private String groupType;

    @Schema(description = "교정 스타일", example = "FORMAL", 
            allowableValues = {"FORMAL", "CASUAL", "LITERARY", "CONCISE"})
    private String correctionStyle;
}