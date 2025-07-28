package com.c203.autobiography.domain.auth.controller;

import com.c203.autobiography.domain.auth.dto.FindEmailRequest;
import com.c203.autobiography.domain.auth.dto.FindEmailResponse;
import com.c203.autobiography.domain.auth.dto.ForgotPasswordRequest;
import com.c203.autobiography.domain.auth.dto.LoginRequest;
import com.c203.autobiography.domain.auth.dto.ResetPasswordRequest;
import com.c203.autobiography.domain.auth.service.AuthService;
import com.c203.autobiography.domain.auth.service.EmailService;
import com.c203.autobiography.domain.member.dto.TokenResponse;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인 후 AccessToken 및 RefreshToken 발급")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @RequestBody @Valid LoginRequest loginRequest, HttpServletRequest httpRequest) {
        TokenResponse tokenResponse = authService.login(loginRequest);
        return ResponseEntity.ok(
                ApiResponse.of(HttpStatus.OK, "로그인 성공", tokenResponse, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "로그아웃", description = "RefreshToken 삭제 (서버에서 무효화)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest
    ) {
        authService.logout(user.getMemberId());
        return ResponseEntity.ok(
                ApiResponse.of(HttpStatus.OK, "로그아웃 성공", null, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "토큰 재발급", description = "RefreshToken으로 새 AccessToken, RefreshToken 발급")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String refreshToken,
            HttpServletRequest httpRequest
    ) {
        TokenResponse token = authService.reissueToken(user.getMemberId(), refreshToken);
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

}
