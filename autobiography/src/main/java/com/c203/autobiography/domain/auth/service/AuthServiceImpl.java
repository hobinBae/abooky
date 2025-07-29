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
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    /** 로그인 */
    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId(), member.getEmail(), String.valueOf(member.getRole()));
        String refreshToken =  jwtTokenProvider.createRefreshToken(member.getMemberId());

        redisTemplate.opsForValue().set("RT:" + member.getMemberId(), refreshToken, 7, TimeUnit.DAYS);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    /** 로그아웃 */
    @Override
    public void logout(Long memberId) {
        redisTemplate.delete("RT:" + memberId);
    }

    /** 토큰 재발급 */
    @Override
    public TokenResponse reissueToken(String refreshToken) {
        // RefreshToken -> 파싱해서 memberId 추출하기
        Claims claims = jwtTokenProvider.parseToken(refreshToken);
        Long memberId;
        try {
            memberId = Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }

        // Redis에서 저장된 refreshToken 비교하기
        String storedToken = redisTemplate.opsForValue().get("RT:" + memberId);
        if(storedToken == null || !storedToken.equals(refreshToken)) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }

        // 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(member.getMemberId(), member.getEmail(), String.valueOf(member.getRole()));
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getMemberId());

        // Redis에 Refresh 토큰 갱신
        redisTemplate.opsForValue().set("RT:" + member.getMemberId(), newRefreshToken, 7, TimeUnit.DAYS);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
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
    public TokenResponse processOAuth2Login(String email, String name, AuthProvider provider, String providerId) {
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

        // TokenResponse 생성
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    private final GoogleOAuth2Service googleService;

    @Override
    public TokenResponse socialLogin(AuthProvider provider, String code) {
        return switch (provider){
            case GOOGLE -> {
                GoogleOAuth2Service.GoogleUserInfo googleUser = null;
                try {
                    googleUser = googleService.getUserInfoFromCode(code);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                yield processOAuth2Login(googleUser.getEmail(), googleUser.getName(), AuthProvider.GOOGLE, googleUser.getProviderId());
            }
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인");
        };
    }
}
