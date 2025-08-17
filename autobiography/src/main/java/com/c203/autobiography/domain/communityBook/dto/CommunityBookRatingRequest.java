package com.c203.autobiography.domain.communityBook.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 평점 생성 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "커뮤니티 책 평점 생성 요청")
public class CommunityBookRatingRequest {

    @NotNull(message = "커뮤니티 책 ID는 필수입니다.")
    @Schema(description = "커뮤니티 책 ID", example = "1", required = true)
    private Long communityBookId;

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    @Schema(description = "평점 (1-5점)", example = "4", required = true)
    private BigDecimal  score;
}