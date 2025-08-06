package com.c203.autobiography.domain.group.dto;

import com.c203.autobiography.domain.group.entity.Group;
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
public class GroupResponse {

    @Schema(description = "그룹 고유 ID", example = "1")
    private Long groupId;

    @Schema(description = "그룹 이름", example = "가족")
    private String groupName;

    @Schema(description = "그룹 방장 ID", example = "1")
    private Long leaderId;

    @Schema(description = "그룹 설명", example = "우리 가족 아카이브")
    private String description;

    @Schema(description = "그룹 테마색", example = "#FFFFFF")
    private String themeColor;

    @Schema(description = "그룹 커버 이미지 URL", example = "https://....jpg")
    private String groupImageUrl;

    @Schema(description = "그룹 생성 일시", example = "2025-07-23T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "그룹 정보 마지막 수정일", example = "2025-07-23T10:00:00")
    private LocalDateTime updatedAt;

    public static GroupResponse from(Group g) {
        return GroupResponse.builder()
                .groupId(g.getGroupId())
                .groupName(g.getGroupName())
                .leaderId(g.getLeaderId())
                .description(g.getDescription())
                .themeColor(g.getThemeColor())
                .groupImageUrl(g.getGroupImageUrl())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}
