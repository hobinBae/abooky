package com.c203.autobiography.domain.book.dto;

import com.c203.autobiography.domain.book.entity.Book;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "책 응답 DTO")
public class BookResponse {
    private Long bookId;
    private Long memberId;
    private String title;
    private String coverImageUrl;
    private String summary;
    private BookType bookType;
    private Long categoryId;
    private String categoryName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer likeCount;
    private Integer viewCount;
    private BigDecimal averageRating; // 소수점 한 자리: "4.5" 형태
    private Boolean completed;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    public static BookResponse of(Book book) {
        return BookResponse.builder()
                .bookId(book.getBookId())
                .memberId(book.getMember().getMemberId())
                .title(book.getTitle())
                .coverImageUrl(book.getCoverImageUrl())
                .summary(book.getSummary())
                .bookType(book.getBookType())
                .categoryId(book.getCategory() != null ? book.getCategory().getBookCategoryId() : null)
                .categoryName(book.getCategory() != null ? book.getCategory().getCategoryName() : null)
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .likeCount(book.getLikeCount())
                .viewCount(book.getViewCount())
                .averageRating(book.getAverageRating())
                .completed(book.getCompleted())
                .completedAt(book.getCompletedAt())
                .build();
    }
}
