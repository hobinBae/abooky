package com.c203.autobiography.domain.communityBook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "커뮤니티 책 댓글 생성 요청 DTO")
public class CommunityBookCommentCreateRequest {
    @NotNull(message = "커뮤니티 책 ID는 필수입니다")
    @Schema(description = "커뮤니티 책 ID", example = "1001", required = true)
    private Long communityBookId;

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 500, message = "댓글은 500자를 초과할 수 없습니다")
    @Schema(description = "댓글 내용", example = "정말 감동적인 책이에요!", required = true)
    private String content;
}
