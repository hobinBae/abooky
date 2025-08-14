package com.c203.autobiography.domain.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProofreadRequest {
    @NotBlank(message = "교정할 텍스트는 비어 있을 수 없습니다..")
    private String textToCorrect;

    @NotNull(message = "책 카테고리는 비어 있을 수 없습니다.")
    private Long bookCategory;
}
