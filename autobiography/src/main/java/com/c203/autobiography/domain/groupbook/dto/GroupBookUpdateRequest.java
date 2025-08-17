package com.c203.autobiography.domain.groupbook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "그룹책 수정 요청 DTO")
public class GroupBookUpdateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 최대 100자까지 가능합니다.")
    private String title;

    @Size(max = 255, message = "커버 이미지 URL은 최대 255자까지 가능합니다.")
    private String coverImageUrl;

    private String summary;

    /** null 허용: 카테고리를 변경하지 않거나 제거할 때 사용 */
    private Long categoryId;

    /*
    // 필요 시 도메인 엔티티에 적용
    public void applyTo(Book book, BookCategory category) {
        book.updateDetails(title, coverImageUrl, summary, category);
    }
    */
}
