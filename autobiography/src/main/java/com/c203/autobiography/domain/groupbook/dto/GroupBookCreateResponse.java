package com.c203.autobiography.domain.groupbook.dto;

import com.c203.autobiography.domain.groupbook.entity.GroupBook;
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
@Schema(description = "그룹책 생성 응답 DTO")
public class GroupBookCreateResponse {

    @Schema(description = "생성된 그룹책 ID", example = "1")
    private Long groupBookId;

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "그룹책 제목", example = "우리 그룹의 이야기")
    private String title;

    @Schema(description = "그룹책 요약", example = "그룹원들이 함께 만든 자서전")
    private String summary;

    @Schema(description = "커버 이미지 URL", example = "https://example.com/cover.jpg")
    private String coverImageUrl;

    @Schema(description = "생성 일시", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "원본 책 ID", example = "42")
    private Long originalBookId;

    @Schema(description = "원본 책 제목", example = "나의 자서전")
    private String originalBookTitle;

    public static GroupBookCreateResponse from(GroupBook groupBook, Long originalBookId, String originalBookTitle) {
        return GroupBookCreateResponse.builder()
                .groupBookId(groupBook.getGroupBookId())
                .groupId(groupBook.getGroup().getGroupId())
                .title(groupBook.getTitle())
                .summary(groupBook.getSummary())
                .coverImageUrl(groupBook.getCoverImageUrl())
                .createdAt(groupBook.getCreatedAt())
                .originalBookId(originalBookId)
                .originalBookTitle(originalBookTitle)
                .build();
    }
}