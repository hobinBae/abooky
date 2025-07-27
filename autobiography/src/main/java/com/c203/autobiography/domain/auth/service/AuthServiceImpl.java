package com.c203.autobiography.domain.auth.service;

import com.c203.autobiography.domain.auth.dto.FindEmailRequest;
import com.c203.autobiography.domain.auth.dto.FindEmailResponse;
import com.c203.autobiography.domain.auth.dto.LoginRequest;
import com.c203.autobiography.domain.auth.dto.ResetPasswordRequest;
import com.c203.autobiography.domain.member.dto.TokenResponse;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import com.c203.autobiography.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements  AuthService {

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

        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId(), member.getEmail());
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
    public TokenResponse reissueToken(Long memberId, String refreshToken) {
        String storedToken = redisTemplate.opsForValue().get("RT:" + memberId);
        if(storedToken == null || !storedToken.equals(refreshToken)) {
            throw new RuntimeException("유효하지 않은 RefreshToken 입니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        String newAccessToken = jwtTokenProvider.createAccessToken(member.getMemberId(), member.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getMemberId());

        redisTemplate.opsForValue().set("RT:" + member.getMemberId(), newAccessToken, 7, TimeUnit.DAYS);

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


}
