package com.c203.autobiography.domain.book.dto;

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
@Schema(description = "책 생성 요청 DTO")
public class BookCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 최대 100자까지 가능합니다.")
    private String title;

    private String summary;

    @NotNull(message = "bookType은 필수입니다.")
    private BookType bookType;

    /** null 허용: 카테고리를 지정하지 않을 수도 있습니다. */
    private Long categoryId;

    /*
    // 필요 시 서비스 계층에서 사용
    public Book toEntity(Member member, BookCategory category) {
        return Book.builder()
            .member(member)
            .title(title)
            .coverImageUrl(coverImageUrl)
            .summary(summary)
            .bookType(bookType)
            .category(category)
            .build();
    }
    */
}
