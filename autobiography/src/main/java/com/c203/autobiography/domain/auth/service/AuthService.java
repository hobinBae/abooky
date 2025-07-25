package com.c203.autobiography.domain.auth.service;

import com.c203.autobiography.domain.auth.dto.LoginRequest;
import com.c203.autobiography.domain.member.dto.TokenResponse;

public interface AuthService {

    /** 로그인 */
    TokenResponse login(LoginRequest loginRequest);

    /** 로그아웃 */
    void logout(Long memberId);

    /** 토큰 재발급 */
    TokenResponse reissueToken(Long memberId, String refreshToken);
}
