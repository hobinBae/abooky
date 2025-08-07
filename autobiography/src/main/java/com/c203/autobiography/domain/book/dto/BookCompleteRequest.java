package com.c203.autobiography.domain.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCompleteRequest {
    @Size(max = 5, message = "태그는 최대 5개까지 가능합니다.")
    private List<
            @NotBlank(message = "태그 이름은 빈 값일 수 없습니다.")
            @Size(max = 50, message = "태그 이름은 최대 50자까지 가능합니다.")
                    String
            > tags;
}