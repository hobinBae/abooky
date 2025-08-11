package com.c203.autobiography.domain.communityBook.dto;

import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "커뮤니티 책 요약 조회 응답")
public class CommunityBookSummaryResponse {
    @Schema(description = "커뮤니티 책 ID", example = "1001")
    private Long communityBookId;

    @Schema(description = "책 제목", example = "나의 첫 번째 자서전")
    private String title;

    @Schema(description = "표지 이미지 URL", example = "https://~~.jpg")
    private String coverImageUrl;

    @Schema(description = "책 요약", example = "책 요약 한 줄")
    private String summary;

    @Schema(description = "책 타입", example = "AUTO")
    private String bookType;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리 이름", example = "자서전")
    private String categoryName;

    @Schema(description = "좋아요 수", example = "10")
    private Integer likeCount;

    @Schema(description = "조회수", example = "20")
    private Integer viewCount;

    @Schema(description = "평균 평점", example = "3.8")
    private BigDecimal averageRating;

    @Schema(description = "완료 여부", example = "true")
    private Boolean completed;

    @Schema(description = "생성 시간", example = "2025-07-20T10:00:00Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2025-07-22T12:30:00Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;

    /**
     * Entity에서 Response 객체로 변환
     */
    public static CommunityBookSummaryResponse of(CommunityBook communityBook) {
        return CommunityBookSummaryResponse.builder()
                .communityBookId(communityBook.getCommunityBookId())
                .title(communityBook.getTitle())
                .coverImageUrl(communityBook.getCoverImageUrl())
                .summary(communityBook.getSummary())
                .bookType(communityBook.getBookType().name())
                .categoryId(communityBook.getCategory() != null ? communityBook.getCategory().getBookCategoryId() : null)
                .categoryName(communityBook.getCategory() != null ? communityBook.getCategory().getCategoryName() : null)
                .likeCount(communityBook.getLikeCount())
                .viewCount(communityBook.getViewCount())
                .averageRating(communityBook.getAverageRating())
                .completed(communityBook.getCompleted())
                .createdAt(communityBook.getCreatedAt())
                .updatedAt(communityBook.getUpdatedAt())
                .build();
    }
}
