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
                .createdAt(episode.getCreatedAt())
                .updatedAt(episode.getUpdatedAt())
                .build();
    }
}
