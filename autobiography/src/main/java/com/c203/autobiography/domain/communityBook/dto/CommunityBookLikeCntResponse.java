package com.c203.autobiography.domain.communityBook.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "커뮤니티 책 좋아요 수 응답")
public class CommunityBookLikeCntResponse {

    @Schema(description = "커뮤니티 책 ID", example = "1")
    private Long communityBookId;

    @Schema(description = "커뮤니티 책 좋아요 수", example = "25")
    private Long communityBookCnt;

    /**
     * 팩토리 메서드
     */
    public static CommunityBookLikeCntResponse of(Long communityBookId, Long communityBookCnt) {
        return CommunityBookLikeCntResponse.builder()
                .communityBookId(communityBookId)
                .communityBookCnt(communityBookCnt)
                .build();
    }
}