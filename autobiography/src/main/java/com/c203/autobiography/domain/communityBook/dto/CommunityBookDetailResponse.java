package com.c203.autobiography.domain.communityBook.dto;

import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookEpisode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "커뮤니티 책 상세 조회 응답")
public class CommunityBookDetailResponse {
    // 기본 정보
    private Long communityBookId;
    private Long memberId;
    private String title;
    private String summary;

    // 카테고리 정보
    private Long categoryId;
    private String categoryName;

    // 책 정보
    private String bookType;
    private String coverImageUrl;
    private Boolean completed;

    // 통계 정보
    private Integer likeCount;
    private Integer viewCount;
    private BigDecimal averageRating;

    // 시간 정보
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;

    // 에피소드 목록
    private List<CommunityBookEpisodeDetailResponse> communityEpisodes;

    /**
     * Entity에서 Response 객체로 변환
     */
    public static CommunityBookDetailResponse of(CommunityBook communityBook, List<CommunityBookEpisode> episodes) {
        return CommunityBookDetailResponse.builder()
                .communityBookId(communityBook.getCommunityBookId())
                .memberId(communityBook.getMember().getMemberId())
                .title(communityBook.getTitle())
                .summary(communityBook.getSummary())
                .categoryId(communityBook.getCategory() != null ? communityBook.getCategory().getBookCategoryId() : null)
                .categoryName(communityBook.getCategory() != null ? communityBook.getCategory().getCategoryName() : null)
                .bookType(communityBook.getBookType().name())
                .coverImageUrl(communityBook.getCoverImageUrl())
                .completed(communityBook.getCompleted())
                .likeCount(communityBook.getLikeCount())
                .viewCount(communityBook.getViewCount())
                .averageRating(communityBook.getAverageRating())
                .createdAt(communityBook.getCreatedAt())
                .updatedAt(communityBook.getUpdatedAt())
                .communityEpisodes(episodes.stream()
                        .map(CommunityBookEpisodeDetailResponse::of)
                        .toList())
                .build();
    }
}
