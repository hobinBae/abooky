package com.c203.autobiography.domain.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "책 좋아요 응답 정보")
public class LikeResponse {

    @Schema(description = "현재 좋아요 여부", example = "true")
    private boolean liked;

    @Schema(description = "현재 총 좋아요 수", example = "55")
    private long likeCount;
}
