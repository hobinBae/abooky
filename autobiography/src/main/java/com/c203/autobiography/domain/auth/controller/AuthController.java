package com.c203.autobiography.domain.auth.controller;

import com.c203.autobiography.domain.auth.dto.FindEmailRequest;
import com.c203.autobiography.domain.auth.dto.FindEmailResponse;
import com.c203.autobiography.domain.auth.dto.ForgotPasswordRequest;
import com.c203.autobiography.domain.auth.dto.LoginRequest;
import com.c203.autobiography.domain.auth.dto.ResetPasswordRequest;
import com.c203.autobiography.domain.auth.service.AuthService;
import com.c203.autobiography.domain.auth.service.EmailService;
import com.c203.autobiography.domain.auth.dto.RefreshTokenRequest;
import com.c203.autobiography.domain.auth.dto.SocialLoginRequest;
import com.c203.autobiography.domain.auth.service.AuthService;
import com.c203.autobiography.domain.member.dto.AuthProvider;
import com.c203.autobiography.domain.member.dto.TokenResponse;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import com.c203.autobiography.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 API", description = "로그인 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인 후 AccessToken 및 RefreshToken 발급")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @RequestBody @Valid LoginRequest loginRequest,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        TokenResponse tokenResponse = authService.login(loginRequest, httpResponse);
        return ResponseEntity.ok(
                ApiResponse.of(HttpStatus.OK, "로그인 성공", tokenResponse, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "로그아웃", description = "RefreshToken 삭제 (서버에서 무효화)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        authService.logout(user.getMemberId());

        // 쿠키 제거
        CookieUtil.deleteRefreshTokenCookie(httpResponse);

        return ResponseEntity.ok(
                ApiResponse.of(HttpStatus.OK, "로그아웃 성공", null, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "토큰 재발급", description = "RefreshToken으로 새 AccessToken, RefreshToken 발급")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @CookieValue(value = CookieUtil.REFRESH_TOKEN_COOKIE, required = false) String rtCookie,
            @RequestBody @Valid RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String refreshToken = (rtCookie != null && !rtCookie.isBlank())
                ? rtCookie
                : (refreshTokenRequest != null ? refreshTokenRequest.getRefreshToken() : null);
        TokenResponse token = authService.reissueToken(refreshToken, httpResponse);
        return ResponseEntity.ok(
                ApiResponse.of(HttpStatus.OK, "토큰 재발급 성공", token, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "비밀번호 재설정 토큰 발급", description = "비밀번호 재설정을 위한 토큰 발급 및 이메일 발송")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request,
                                                            HttpServletRequest httpRequest) {
        emailService.sendPasswordResetEmail(request);
        return ResponseEntity.ok(
                ApiResponse.of(HttpStatus.OK, "비밀번호 재설정 이메일이 전송되었습니다.", null, httpRequest.getRequestURI()));
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호 재설정 토큰으로 비밀번호 재설정")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequest request,
                                                           HttpServletRequest httpRequest) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.of(HttpStatus.OK, "비밀번호가 성공적으로 변경되었습니다.", null, httpRequest.getRequestURI()));
    }

    @Operation(summary = "이메일 찾기", description = "사용자 이름과 전화번호로 이메일 찾기")
    @PostMapping("/find-email")
    public ResponseEntity<ApiResponse<FindEmailResponse>> findEmail(@RequestBody @Valid FindEmailRequest request,
                                                                    HttpServletRequest httpRequest) {
        FindEmailResponse response = authService.findEmail(request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "이메일 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "소셜 로그인", description = "구글 계정으로 로그인할 수 있습니다.")
    @PostMapping("/oauth2")
    public ResponseEntity<ApiResponse<TokenResponse>> socialLogin(
            @RequestBody @Valid SocialLoginRequest socialLoginRequest, HttpServletRequest httpRequest) throws Exception {
        TokenResponse tokenResponse = authService.socialLogin(socialLoginRequest.getProvider(),  socialLoginRequest.getCode());
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, socialLoginRequest.getProvider() + " 로그인 성공", tokenResponse, httpRequest.getRequestURI()));
    }


}
