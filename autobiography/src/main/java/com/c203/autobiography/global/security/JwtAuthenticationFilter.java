package com.c203.autobiography.global.security;

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

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if(header!=null && header.startsWith("Bearer "))
        {
            String token = header.substring(7);
            try {
                Claims claims = jwtTokenProvider.parseToken(token);
                Long memberId = Long.valueOf(claims.getSubject());
                String email = claims.get("email").toString();
                Role role = Role.valueOf(claims.get("role", String.class));

                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

                CustomUserDetails customUserDetails = new CustomUserDetails(memberId, email, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(customUserDetails,null, customUserDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException e) {
                throw new AuthenticationException("JWT 인증 실패") {};
            }
        }
        filterChain.doFilter(request, response);
    }
}
