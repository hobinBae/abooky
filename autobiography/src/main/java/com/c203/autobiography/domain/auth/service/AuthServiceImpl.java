package com.c203.autobiography.domain.auth.service;

import com.c203.autobiography.domain.auth.dto.FindEmailRequest;
import com.c203.autobiography.domain.auth.dto.FindEmailResponse;
import com.c203.autobiography.domain.auth.dto.LoginRequest;
import com.c203.autobiography.domain.auth.dto.ResetPasswordRequest;
import com.c203.autobiography.domain.member.dto.AuthProvider;
import com.c203.autobiography.domain.member.dto.Role;
import com.c203.autobiography.domain.member.dto.TokenResponse;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import com.c203.autobiography.global.security.jwt.JwtTokenProvider;
import com.c203.autobiography.global.security.oauth2.GoogleOAuth2Service;
import com.c203.autobiography.global.util.CookieUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    /** 로그인 */
    @Override
    public TokenResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId(), member.getEmail(), String.valueOf(member.getRole()));
        String refreshToken =  jwtTokenProvider.createRefreshToken(member.getMemberId());

        redisTemplate.opsForValue().set("RT:" + member.getMemberId(), refreshToken, 7, TimeUnit.DAYS);

        // 쿠키 추가
        CookieUtil.addRefreshTokenCookie(response, refreshToken, 7);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .build();

    }

    /** 로그아웃 */
    @Override
    public void logout(Long memberId) {
        redisTemplate.delete("RT:" + memberId);
    }


    @Override
    public TokenResponse reissueToken(String refreshToken, HttpServletResponse response) {
        log.debug("[reissueToken] called. cookieRT.len={}", refreshToken == null ? null : refreshToken.length());

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("[reissueToken] missing refreshToken (cookie null/blank)");
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }

        // 1) 파싱/검증
        Claims claims;
        try {
            claims = jwtTokenProvider.parseToken(refreshToken);
        } catch (Exception ex) {
            log.warn("[reissueToken] parseToken failed: {}", ex.toString());
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId;
        try {
            memberId = Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            log.warn("[reissueToken] invalid subject: {}", claims.getSubject());
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }

        // 2) Redis에서 RT 조회 및 재사용/불일치 탐지
        String key = "RT:" + memberId;
        String storedToken = redisTemplate.opsForValue().get(key);
        if (storedToken == null) {
            log.warn("[reissueToken] redis missing. key={}", key);
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
        if (!storedToken.equals(refreshToken)) {
            // 토큰 재사용 의심 → 세션 강제 종료(키 삭제) 등 방어
            redisTemplate.delete(key);
            log.warn("[reissueToken] RT mismatch detected. key={} (possible reuse/hijack)", key);
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }

        // 3) 회원 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 4) 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(
                member.getMemberId(), member.getEmail(), String.valueOf(member.getRole()));
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getMemberId());

        // 5) TTL 동기화: RT 만료와 동일하게 설정(권장)
        long secondsLeft;
        try {
            Date exp = jwtTokenProvider.parseToken(newRefreshToken).getExpiration();
            secondsLeft = Math.max(0, (exp.getTime() - System.currentTimeMillis()) / 1000);
        } catch (Exception e) {
            // 실패 시 fallback: 7일
            secondsLeft = TimeUnit.DAYS.toSeconds(7);
        }

        // 6) Redis 갱신(로테이션)
        redisTemplate.opsForValue().set(key, newRefreshToken, secondsLeft, TimeUnit.SECONDS);

        // 7) 쿠키 갱신
        try {
            CookieUtil.addRefreshTokenCookie(response, newRefreshToken, TimeUnit.SECONDS.toDays(secondsLeft));
        } catch (Exception e) {
            log.error("[reissueToken] failed to set RT cookie: {}", e.toString());
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        log.debug("[reissueToken] success. memberId={}, ttlSec={}", memberId, secondsLeft);
        return TokenResponse.builder().accessToken(newAccessToken).build();
    }


    /** 비밀번호 재발급 */
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String tokenKey = "reset:" + request.getResetToken();
        String memberId = redisTemplate.opsForValue().get(tokenKey);

        if (memberId == null) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
        Member member = memberRepository.findById(Long.parseLong(memberId))
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        member.changePassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);
        redisTemplate.delete(tokenKey);
    }

    @Override
    public FindEmailResponse findEmail(FindEmailRequest request) {
        Member member = memberRepository.findByNameAndPhoneNumber(request.getName(), request.getPhoneNumber())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return FindEmailResponse.builder().email(member.getEmail())
                .build();
    }


    @Override
    public TokenResponse processOAuth2Login(String email, String name, AuthProvider provider, String providerId, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(email)
                .orElseGet(()-> memberRepository.save(Member.builder()
                        .email(email)
                        .name(name)
                        .provider(provider)
                        .providerId(providerId)
                        .role(Role.MEMBER)
                        .build()
                ));

        // JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId(), member.getEmail(), String.valueOf(member.getRole()));
        String refreshToken =  jwtTokenProvider.createRefreshToken(member.getMemberId());

        // Redis 저장
        redisTemplate.opsForValue().set("RT:" + member.getMemberId(), refreshToken, 7, TimeUnit.DAYS);

        // 쿠키 저장
        CookieUtil.addRefreshTokenCookie(response, refreshToken, 7);

        // TokenResponse 생성
        return TokenResponse.builder()
                .accessToken(accessToken)
                .build();

    }

    private final GoogleOAuth2Service googleService;

    @Override
    public TokenResponse socialLogin(AuthProvider provider, String code, HttpServletResponse httpResponse) {
        return switch (provider){
            case GOOGLE -> {
                GoogleOAuth2Service.GoogleUserInfo googleUser = null;
                try {
                    googleUser = googleService.getUserInfoFromCode(code);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                yield processOAuth2Login(googleUser.getEmail(), googleUser.getName(), AuthProvider.GOOGLE, googleUser.getProviderId(), httpResponse
                        );
            }
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인");
        };
    }
}
