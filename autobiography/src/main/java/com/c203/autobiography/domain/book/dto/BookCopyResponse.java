package com.c203.autobiography.domain.book.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "다른 이름으로 복사된 책 응답 DTO")
public class BookCopyResponse {

    @Schema(description = "새로 생성된 책 ID", example = "42")
    private Long bookId;

    @Schema(description = "새로 생성된 책 제목", example = "공유용 나의 자서전")
    private String title;

    @Schema(description = "새로 생성된 책 요약", example = "커뮤니티에 공유하는 버전입니다.")
    private String summary;

    @Schema(description = "새로 생성된 책 카테고리 ID (선택)", example = "3")
    private Long categoryId;

    @Schema(description = "생성일시")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

}
