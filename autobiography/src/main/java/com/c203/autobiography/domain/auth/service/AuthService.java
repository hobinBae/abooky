package com.c203.autobiography.domain.auth.service;

import com.c203.autobiography.domain.auth.dto.FindEmailRequest;
import com.c203.autobiography.domain.auth.dto.FindEmailResponse;
import com.c203.autobiography.domain.auth.dto.LoginRequest;
import com.c203.autobiography.domain.auth.dto.ResetPasswordRequest;
import com.c203.autobiography.domain.member.dto.AuthProvider;
import com.c203.autobiography.domain.member.dto.TokenResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    /** 로그인 */
    TokenResponse login(LoginRequest loginRequest, HttpServletResponse response);

    /** 로그아웃 */
    void logout(Long memberId);

    /** 비밀번호 재설정 */
    void resetPassword(ResetPasswordRequest request);

    /** 이메일 찾기 */
    FindEmailResponse findEmail(FindEmailRequest request);

    /** 토큰 재발급 */
    TokenResponse reissueToken(String refreshToken, HttpServletResponse response);

    /** OAuth2 로그인 공통 처리 */
    TokenResponse processOAuth2Login(String email, String name, AuthProvider provider, String providerId, HttpServletResponse response);

    /** 소셜 로그인 */
    TokenResponse socialLogin(AuthProvider provider, String code);
}
