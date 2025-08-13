package com.c203.autobiography.domain.groupbook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "댓글 삭제 응답")
public class GroupBookCommentDeleteResponse {
    @Schema(description = "삭제된 댓글 ID", example = "1")
    private Long deletedCommentId;

    public static GroupBookCommentDeleteResponse of(Long commentId) {
        return GroupBookCommentDeleteResponse.builder()
                .deletedCommentId(commentId)
                .build();
    }
}
