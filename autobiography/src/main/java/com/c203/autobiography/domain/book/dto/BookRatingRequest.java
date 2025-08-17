package com.c203.autobiography.domain.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRatingRequest {

    @Schema(description = "평점(1~5 정수)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(1) @Max(5)
    private Integer score;
}
