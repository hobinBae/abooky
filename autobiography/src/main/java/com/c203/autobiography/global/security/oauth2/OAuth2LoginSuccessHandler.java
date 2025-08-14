package com.c203.autobiography.global.security.oauth2;

import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.security.jwt.JwtTokenProvider;
import com.c203.autobiography.global.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${app.oauth2.authorizedRedirectUri:http://localhost:3000/auth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        // 이미 CustomOAuth2UserService에서 생성된 Member 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OAuth2 사용자를 찾을 수 없습니다."));

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId(), member.getEmail(), String.valueOf(member.getRole()));
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getMemberId());

        // Redis에 RefreshToken 저장
        redisTemplate.opsForValue().set("RT:" + member.getMemberId(), refreshToken, 7, TimeUnit.DAYS);
        
        // RefreshToken을 쿠키로 저장
        CookieUtil.addRefreshTokenCookie(response, refreshToken, 7);

        // 프론트엔드 콜백 URL로 리다이렉트 (토큰 포함)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .build().toUriString();
                
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
