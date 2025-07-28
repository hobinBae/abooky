package com.c203.autobiography.domain.auth.controller;

import com.c203.autobiography.domain.member.dto.AuthProvider;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.dto.Role;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String EMAIL = "login@test.com";
    private static final String PASSWORD = "testPass123!";

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        memberRepository.save(Member.builder()
                .email(EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .name("홍길동")
                .nickname("길동이")
                .role(Role.MEMBER)
                .provider(AuthProvider.LOCAL)
                .build());
    }

    @Test
    void 로그인_재발급_로그아웃_전체_시나리오() throws Exception {
        // 1) 로그인 → AccessToken, RefreshToken 발급
        String loginJson = """
        {
          "email": "login@test.com",
          "password": "testPass123!"
        }
        """;

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = objectMapper.readTree(loginResponse).path("data").path("accessToken").asText();
        String refreshToken = objectMapper.readTree(loginResponse).path("data").path("refreshToken").asText();
        assertThat(accessToken).isNotEmpty();
        assertThat(refreshToken).isNotEmpty();

        // 2) RefreshToken으로 토큰 재발급
        String refreshJson = """
        {
          "refreshToken": "%s"
        }
        """.formatted(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());

        // 3) 로그아웃 (AccessToken 필요)
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 성공"));

        // 4) 로그아웃 후 RefreshToken으로 재발급 시도 → 실패 (예: 401)
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isUnauthorized());
    }
}
