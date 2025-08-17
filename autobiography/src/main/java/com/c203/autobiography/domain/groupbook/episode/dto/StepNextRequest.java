package com.c203.autobiography.domain.groupbook.episode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepNextRequest {

    @Schema(description = "마지막 완료한 스텝 번호", example = "1")
    private Integer lastStepNo;

    @NotBlank
    @Schema(description = "사용자 답변", example = "우리 가족은 아버지, 어머니, 그리고 저 이렇게 3명입니다.")
    private String userAnswer;

    @Schema(description = "글 톤 스타일", example = "PLAIN", allowableValues = {"PLAIN", "FORMAL", "CASUAL"})
    @Builder.Default
    private String tone = "PLAIN";
}
