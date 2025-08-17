package com.c203.autobiography.domain.episode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "에피소드 이미지 업로드 요청 DTO")
public class EpisodeImageUploadRequest {

    @Schema(description = "이미지 설명", example = "가족 사진")
    private String description;

    @Schema(description = "이미지 순서 (선택사항, 없으면 자동 부여)", example = "1")
    private Integer orderNo;
}