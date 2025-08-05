package com.c203.autobiography.domain.book.dto;


import com.c203.autobiography.domain.book.entity.BookCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "책 응답 DTO")
public class BookCategoryResponse {
    private Long bookCategoryId;
    private String categoryName;

    public static BookCategoryResponse of(BookCategory category) {
        return BookCategoryResponse.builder()
                .bookCategoryId(category.getBookCategoryId())
                .categoryName(category.getCategoryName())
                .build();
    }
}
