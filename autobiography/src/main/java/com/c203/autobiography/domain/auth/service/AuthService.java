package com.c203.autobiography.domain.auth.service;

import com.c203.autobiography.domain.auth.dto.LoginRequest;
import com.c203.autobiography.domain.member.dto.AuthProvider;
import com.c203.autobiography.domain.member.dto.TokenResponse;

public interface AuthService {

    /** 로그인 */
    TokenResponse login(LoginRequest loginRequest);

    /** 로그아웃 */
    void logout(Long memberId);

    /** 토큰 재발급 */
    TokenResponse reissueToken(String refreshToken);

    /** OAuth2 로그인 공통 처리 */
    TokenResponse processOAuth2Login(String email, String name, AuthProvider provider, String providerId);

    /** 소셜 로그인 */
    TokenResponse socialLogin(AuthProvider provider, String code);
}
