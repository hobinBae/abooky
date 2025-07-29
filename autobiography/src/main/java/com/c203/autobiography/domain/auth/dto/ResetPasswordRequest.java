package com.c203.autobiography.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {



    @Schema(description = "이메일로 전송된 비밀번호 재설정 토큰", example = "abc123-uuid=token")
    @NotBlank(message = "토큰은 필수입니다.")
    private String resetToken;

    @Schema(description = "새 비밀번호 (8자 이상)", example = "newPassword123!@")
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    private String newPassword;
}
