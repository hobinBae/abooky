package com.c203.autobiography.global.security.jwt;

import com.c203.autobiography.domain.member.dto.Role;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // OAuth2 로그인 관련 경로는 JWT 필터에서 제외
        String uri = request.getRequestURI();
        System.out.println(">> JWTFilter 실행 URI: " + uri);

        if (uri.startsWith("/login/oauth2/") ||
                uri.startsWith("/login/oauth2/code") ||
                uri.startsWith("/oauth2/authorization/") ||
                uri.startsWith("/oauth2/callback/")) {
            System.out.println(">> OAuth 경로 - JWT 필터 통과");
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        String header = request.getHeader("Authorization");
        System.out.println(">> Authorization 헤더: " + header);

        // 1. 헤더에서 토큰을 먼저 찾습니다.
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }
        // 2. 헤더에 토큰이 없고, SSE 스트림 요청인 경우에만 URL 파라미터에서 찾습니다.
        else if (uri.contains("/stream")) {
            token = request.getParameter("token");
            System.out.println(">> SSE 스트림 요청 - URL 파라미터에서 토큰 추출 시도: " + (token != null));
        }

        // JWT 검증 (토큰이 어떤 방식으로든 발견된 경우에만 실행)
        if (token != null) {
            try {
                Claims claims = jwtTokenProvider.parseToken(token);
                Long memberId = Long.valueOf(claims.getSubject());
                String email = claims.get("email").toString();
                Role role = Role.valueOf(claims.get("role", String.class));

                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

                CustomUserDetails customUserDetails = new CustomUserDetails(memberId, email, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (JwtException e) {
                // 기존 에러 처리 방식을 유지합니다.
                throw new AuthenticationException("JWT 인증 실패") {};
            }
        }
        filterChain.doFilter(request, response);
    }
}
