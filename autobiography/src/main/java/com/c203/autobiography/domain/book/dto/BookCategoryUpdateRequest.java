package com.c203.autobiography.domain.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "책 카테고리 수정 DTO")
public class BookCategoryUpdateRequest {
    @NotBlank(message = "카테고리 이름은 필수입니다.")
    @Size(max = 50, message = "카테고리 이름은 최대 50자까지 가능합니다.")
    private String categoryName;
    /*
    // 서비스 레이어에서 사용
    public void applyTo(BookCategory category) {
        category.updateName(categoryName);
    }
    */

}
