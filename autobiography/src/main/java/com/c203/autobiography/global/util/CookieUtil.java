package com.c203.autobiography.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtil {
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    public static void addRefreshTokenCookie(HttpServletResponse response, String token, long days) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(false)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(days))
//                .domain(".domain.com")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("None")
                .path("/")
                .maxAge(0).build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
