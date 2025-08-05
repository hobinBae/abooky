package com.c203.autobiography.domain.book.dto;

import com.c203.autobiography.domain.episode.dto.EpisodeCopyRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "다른 이름으로 책을 복사하기 위한 요청 DTO")
public class BookCopyRequest {

    @NotBlank(message = "책 제목은 필수입니다.")
    @Size(max = 100, message = "책 제목은 최대 100자까지 가능합니다.")
    @Schema(description = "새로 생성할 책의 제목", example = "공유용 나의 자서전")
    private String title;

    @Size(max = 1000, message = "요약은 최대 1000자까지 가능합니다.")
    @Schema(description = "새로 생성할 책의 요약", example = "커뮤니티에 공유하는 버전입니다.")
    private String summary;

    @Schema(description = "새로 생성할 책의 카테고리 ID (선택)", example = "3")
    private Long categoryId;

    @NotEmpty(message = "최소 하나 이상의 에피소드를 지정해야 합니다.")
    @Valid  // 중첩된 EpisodeCopyRequest 검증
    @Schema(description = "복제 및 수정/삭제할 에피소드 목록")
    private List<EpisodeCopyRequest> episodes;

}
