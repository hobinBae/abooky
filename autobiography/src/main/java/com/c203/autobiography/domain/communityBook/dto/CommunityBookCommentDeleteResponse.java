package com.c203.autobiography.domain.communityBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "댓글 삭제 응답")
public class CommunityBookCommentDeleteResponse {
    @Schema(description = "삭제된 댓글 ID", example = "1")
    private Long deletedCommentId;

    public static CommunityBookCommentDeleteResponse of(Long commentId) {
        return CommunityBookCommentDeleteResponse.builder()
                .deletedCommentId(commentId)
                .build();
    }
}
