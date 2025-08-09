package com.c203.autobiography.global.security.oauth2;

import com.c203.autobiography.domain.auth.service.AuthService;
import com.c203.autobiography.domain.member.dto.AuthProvider;
import com.c203.autobiography.domain.member.dto.Role;
import com.c203.autobiography.domain.member.dto.TokenResponse;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        System.out.println(">> OAuth2 성공 핸들러 호출됨");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        System.out.println(">> OAuth2 User Attributes: " + oAuth2User.getAttributes());

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        AuthProvider provider = AuthProvider.GOOGLE;
        String providerId = oAuth2User.getName();

        // 임의 닉네임
        String rawNickname = name != null ? name : email.split("@")[0];
        String nickname = rawNickname + "_" + System.currentTimeMillis() % 10000; // 중복 방지

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .email(email)
                        .name(name)
                        .nickname(nickname)
                        .provider(provider)
                        .providerId(providerId)
                        .role(Role.MEMBER)
                        .build()));

        // 2. JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId(), member.getEmail(), String.valueOf(member.getRole()));
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getMemberId());

        // 3. Redis에 RefreshToken 저장
        redisTemplate.opsForValue().set("RT:" + member.getMemberId(), refreshToken, 7, TimeUnit.DAYS);

        // 4. 응답 작성
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(accessToken)
                .build();

        ApiResponse<TokenResponse> responseBody = ApiResponse.of(
                HttpStatus.OK, "소셜 로그인 성공", tokenResponse, request.getRequestURI()
        );

        // JSON 응답
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.OK.value());
        new ObjectMapper().writeValue(response.getOutputStream(), responseBody);
    }
}
