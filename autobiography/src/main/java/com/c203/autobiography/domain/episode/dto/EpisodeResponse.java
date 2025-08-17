package com.c203.autobiography.domain.episode.dto;

import com.c203.autobiography.domain.episode.entity.Episode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "에피소드 응답 DTO")
public class EpisodeResponse {
    @Schema(description = "에피소드 ID", example = "1001")
    private Long episodeId;

    @Schema(description = "책 ID (Book)", example = "5001")
    private Long bookId;

    @Schema(description = "에피소드 제목", example = "유년기의 추억")
    private String title;

    @Schema(description = "에피소드 날짜", example = "1990-05-17")
    private LocalDate episodeDate;

    @Schema(description = "에피소드 순서", example = "1")
    private Integer episodeOrder;

    @Schema(description = "에피소드 본문 내용", example = "광주에서 태어난 나는 ...")
    private String content;

    @Schema(description = "저장된 오디오 URL", example = "https://.../audio.webm")
    private String audioUrl;

    @Schema(description = "해당 에피소드에서 진행 중인 대화 세션 ID (없으면 null)", nullable = true)
    private String activeSessionId;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    public static EpisodeResponse of(Episode e){
        return EpisodeResponse.builder()
                .episodeId(e.getEpisodeId())
                .bookId(e.getBook().getBookId())
                .title(e.getTitle())
                .content(e.getContent())
                .episodeOrder(e.getEpisodeOrder())
                .episodeDate(e.getEpisodeDate())
                .audioUrl(e.getAudioUrl())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
    public static EpisodeResponse of(Episode e, String activeSessionId) {
        // 기존 of 메소드를 호출하여 기본 정보를 채웁니다.
        EpisodeResponse response = of(e);
        // activeSessionId 필드를 추가로 설정합니다.
        response.setActiveSessionId(activeSessionId);
        return response;
    }

}
