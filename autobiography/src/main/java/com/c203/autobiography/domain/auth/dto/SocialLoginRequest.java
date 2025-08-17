package com.c203.autobiography.domain.auth.dto;

import com.c203.autobiography.domain.member.dto.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {
    @NotBlank
    private String code;  // Authorization Code

    @NotNull
    private AuthProvider provider;  // GOOGLE, KAKAO, NAVER
}
