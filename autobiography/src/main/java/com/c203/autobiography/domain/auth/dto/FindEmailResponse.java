package com.c203.autobiography.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindEmailResponse {

    @Schema(description = "회원 이메일", example = "user@example.com")
    private String email;
}
