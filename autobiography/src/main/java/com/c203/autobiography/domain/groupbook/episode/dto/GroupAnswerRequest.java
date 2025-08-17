package com.c203.autobiography.domain.groupbook.episode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹 에피소드 답변 요청 DTO")
public class GroupAnswerRequest {
    
    @NotBlank(message = "답변은 필수입니다.")
    @Size(min = 1, max = 5000, message = "답변은 1자 이상 5000자 이하로 입력해주세요.")
    @Schema(description = "사용자 답변", example = "우리 가족은 아버지, 어머니, 형, 그리고 저 이렇게 4명으로 구성되어 있습니다.")
    private String answer;
    
    @Schema(description = "답변하는 질문의 키", example = "INTRO_FAMILY_1")
    private String questionKey;
    
    @Schema(description = "현재 템플릿", example = "INTRO")
    private String currentTemplate;
    
    @Schema(description = "현재 단계", example = "1")
    private Integer currentStep;
}