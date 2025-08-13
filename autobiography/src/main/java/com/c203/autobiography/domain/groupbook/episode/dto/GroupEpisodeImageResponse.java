package com.c203.autobiography.domain.groupbook.episode.dto;

import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisodeImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹 에피소드 이미지 응답 DTO")
public class GroupEpisodeImageResponse {

    @Schema(description = "그룹 에피소드 ID", example = "1")
    private Long groupEpisodeId;

    @Schema(description = "이미지 ID", example = "1")
    private Long imageId;

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "이미지 순서", example = "1")
    private Integer orderNo;

    @Schema(description = "이미지 설명", example = "가족 사진")
    private String description;

    @Schema(description = "생성 일시", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    public static GroupEpisodeImageResponse from(GroupEpisodeImage image) {
        return GroupEpisodeImageResponse.builder()
                .groupEpisodeId(image.getId().getGroupEpisodeId())
                .imageId(image.getId().getImageId())
                .imageUrl(image.getImageUrl())
                .orderNo(image.getOrderNo())
                .description(image.getDescription())
                .createdAt(image.getCreatedAt())
                .build();
    }
}