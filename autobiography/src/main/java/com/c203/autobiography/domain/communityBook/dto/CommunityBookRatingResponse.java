package com.c203.autobiography.domain.communityBook.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 평점 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "커뮤니티 책 평점 응답")
public class CommunityBookRatingResponse {

    @Schema(description = "커뮤니티 책 ID", example = "1")
    private Long communityBookId;

    @Schema(description = "평점", example = "4")
    private BigDecimal score;

    /**
     * 팩토리 메서드
     */
    public static CommunityBookRatingResponse of(Long communityBookId, BigDecimal score) {
        return CommunityBookRatingResponse.builder()
                .communityBookId(communityBookId)
                .score(score)
                .build();
    }
}