package com.c203.autobiography.domain.groupbook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹북 댓글 리스트 조회 응답")
public class GroupBookCommentListResponse {
    @Schema(description = "댓글 목록")
    private List<GroupBookCommentDetailResponse> content;

    @Schema(description = "페이징 정보")
    private PageableInfo pageable;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "페이징 정보")
    public static class PageableInfo {

        @Schema(description = "현재 페이지 번호", example = "0")
        private int page;

        @Schema(description = "페이지 크기", example = "20")
        private int size;

        @Schema(description = "전체 요소 수", example = "100")
        private long totalElements;

        @Schema(description = "전체 페이지 수", example = "5")
        private int totalPages;

        public static PageableInfo of(org.springframework.data.domain.Page<?> page) {
            return PageableInfo.builder()
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .build();
        }
    }

    public static GroupBookCommentListResponse of(org.springframework.data.domain.Page<GroupBookCommentDetailResponse> page) {
        return GroupBookCommentListResponse.builder()
                .content(page.getContent())
                .pageable(PageableInfo.of(page))
                .build();
    }
}