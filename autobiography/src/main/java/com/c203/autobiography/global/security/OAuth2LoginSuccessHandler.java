package com.c203.autobiography.global.security;

import com.c203.autobiography.domain.member.dto.AuthProvider;
import com.c203.autobiography.domain.member.dto.Role;
import com.c203.autobiography.domain.member.dto.TokenResponse;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        AuthProvider provider = AuthProvider.GOOGLE;
        String providerId = oAuth2User.getName();

        //DB에 회원 없으면 새로 가입
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

        // TokenResponse 생성
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
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
