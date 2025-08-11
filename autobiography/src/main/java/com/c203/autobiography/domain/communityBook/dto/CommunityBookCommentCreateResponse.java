package com.c203.autobiography.domain.communityBook.dto;

import com.c203.autobiography.domain.communityBook.entity.CommunityBookComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "커뮤니티 책 댓글 응답 DTO")
public class CommunityBookCommentCreateResponse {
    @Schema(description = "커뮤니티 책 댓글 ID", example = "501")
    private Long communityBookCommentId;

    @Schema(description = "커뮤니티 책 ID", example = "1001")
    private Long communityBookId;

    @Schema(description = "댓글 내용", example = "정말 감동적인 책이에요!")
    private String content;

    @Schema(description = "작성일시", example = "2025-07-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "작성자 ID", example = "123")
    private Long memberId;

    @Schema(description = "수정일시", example = "2025-07-22T10:30:00")
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환 메서드 (팀 컨벤션에 맞춰 of 사용)
    public static CommunityBookCommentCreateResponse of(CommunityBookComment comment) {
        return CommunityBookCommentCreateResponse.builder()
                .communityBookCommentId(comment.getCommunityBookCommentId())
                .communityBookId(comment.getCommunityBook().getCommunityBookId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .memberId(comment.getMember().getMemberId())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
