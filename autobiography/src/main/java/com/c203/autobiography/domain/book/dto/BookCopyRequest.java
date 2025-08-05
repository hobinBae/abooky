package com.c203.autobiography.domain.book.dto;

import com.c203.autobiography.domain.episode.dto.EpisodeCopyRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "다른 이름으로 책을 복사합니다.")
public class BookCopyRequest {

    @NotBlank(message = "책 제목은 필수입니다.")
    private String title;

    private String summary;

    private Long categoryId;

    @NotEmpty(message = "반드시 하나 이상의 에피소드를 지정해야 합니다.")
    private List<EpisodeCopyRequest> episodes;


}
