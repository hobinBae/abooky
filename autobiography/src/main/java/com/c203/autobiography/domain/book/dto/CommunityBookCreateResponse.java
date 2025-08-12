package com.c203.autobiography.domain.book.dto;

import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "책 커뮤니티 내보내기 응답")
public class CommunityBookCreateResponse {
    @Schema(description = "원본 책 ID", example = "123")
    private Long originalBookId;

    @Schema(description = "생성된 커뮤니티 책 ID", example = "456")
    private Long communityBookId;

    @Schema(description = "커뮤니티 책 제목", example = "내 인생 이야기")
    private String title;

    @Schema(description = "복사된 에피소드 수", example = "15")
    private int episodeCount;

    @Schema(description = "내보내기 완료 메시지", example = "커뮤니티 책으로 성공적으로 내보내기가 완료되었습니다.")
    private String message;

    public static CommunityBookCreateResponse of(Long originalBookId, CommunityBook communityBook, int episodeCount) {
        return CommunityBookCreateResponse.builder()
                .originalBookId(originalBookId)
                .communityBookId(communityBook.getCommunityBookId())
                .title(communityBook.getTitle())
                .episodeCount(episodeCount)
                .message("커뮤니티 책으로 성공적으로 내보내기가 완료되었습니다.")
                .build();
    }
}
