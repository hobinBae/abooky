package com.c203.autobiography.domain.communityBook.dto;

import com.c203.autobiography.domain.communityBook.entity.CommunityBookComment;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "커뮤니티 책 댓글 조회 응답")
public class CommunityBookCommentDetailResponse {
    @Schema(description = "댓글 ID", example = "501")
    private Long communityBookCommentId;

    @Schema(description = "작성자 ID", example = "101")
    private Long memberId;

    @Schema(description = "작성자 닉네임", example = "독서광")
    private String nickname;

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://~~.jpg")
    private String profileImageUrl;

    @Schema(description = "댓글 내용", example = "정말 감동적인 책이에요!")
    private String content;

    @Schema(description = "작성일시", example = "2025-07-23T10:31:00Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2025-07-23T10:31:00Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환 메서드 (팀 컨벤션에 맞춰 of 사용)
    public static CommunityBookCommentDetailResponse of(CommunityBookComment comment) {
        return CommunityBookCommentDetailResponse.builder()
                .communityBookCommentId(comment.getCommunityBookCommentId())
                .memberId(comment.getMember().getMemberId())
                .nickname(comment.getMember().getNickname())
                .profileImageUrl(comment.getMember().getProfileImageUrl())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
