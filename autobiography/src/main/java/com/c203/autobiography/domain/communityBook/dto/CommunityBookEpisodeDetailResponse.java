package com.c203.autobiography.domain.communityBook.dto;

import com.c203.autobiography.domain.communityBook.entity.CommunityBookEpisode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "커뮤니티 책 에피소드 상세 조회 응답")
public class CommunityBookEpisodeDetailResponse {
    private Long communityEpisodeId;
    private Long communityBookId;
    private String title;
    private String content;
    private LocalDate episodeDate;
    private Integer episodeOrder;

    // 태그 정보 (현재 스키마에는 없지만 응답 예시에 있으므로 포함)
    private List<CommunityBookEpisodeTagResponse> tags;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;

    /**
     * Entity에서 Response 객체로 변환
     */
    public static CommunityBookEpisodeDetailResponse of(CommunityBookEpisode episode) {
        return CommunityBookEpisodeDetailResponse.builder()
                .communityEpisodeId(episode.getCommunityEpisodeId())
                .communityBookId(episode.getCommunityBook().getCommunityBookId())
                .title(episode.getTitle())
                .content(episode.getContent())
                .episodeDate(episode.getEpisodeDate())
                .episodeOrder(episode.getEpisodeOrder())
                .tags(createDummyTags()) // 실제 태그 시스템이 있다면 여기서 조회
                .createdAt(episode.getCreatedAt())
                .updatedAt(episode.getUpdatedAt())
                .build();
    }

    /**
     * 임시 태그 데이터 (실제 태그 시스템 구현 시 제거)
     */
    private static List<CommunityBookEpisodeTagResponse> createDummyTags() {
        return List.of(
                CommunityBookEpisodeTagResponse.builder().communityBookEpisodeTagId(10L).communityBookEpisodeTagName("추억").build(),
                CommunityBookEpisodeTagResponse.builder().communityBookEpisodeTagId(11L).communityBookEpisodeTagName("가족").build(),
                CommunityBookEpisodeTagResponse.builder().communityBookEpisodeTagId(12L).communityBookEpisodeTagName("여행").build()
        );
    }

}
