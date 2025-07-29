package com.c203.autobiography.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshTokenRequest {
    @Schema(description = "Refresh Token", example ="eyJhbGciOi...")
    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;
}
