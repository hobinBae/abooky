package com.c203.autobiography.domain.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRatingResponse {

    @Schema(description = "책 ID", example = "1")
    private Long bookId;

    @Schema(description = "내 평점(1~5 정수)", example = "4")
    private Integer myScore;

    @Schema(description = "책 평균 평점(소수점 1자리)", example = "4.3")
    private BigDecimal averageRating;

    @Schema(description = "총 평점 수", example = "12")
    private Long ratingCount;
}
