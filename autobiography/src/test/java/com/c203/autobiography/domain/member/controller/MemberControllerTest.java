package com.c203.autobiography.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.c203.autobiography.domain.member.service.MemberService;
import com.c203.autobiography.domain.member.dto.MemberCreateRequest;
import com.c203.autobiography.domain.member.dto.MemberResponse;
import com.c203.autobiography.global.s3.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MemberController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private FileStorageService fileStorageService;

    @Test
    void register_회원가입_성공() throws Exception {
        // given
        MemberCreateRequest request = MemberCreateRequest.builder()
                .email("test@example.com")
                .password("testPass123!")
                .name("홍길동")
                .nickname("길동이")
                .phoneNumber("010-1234-5678")
                .birthdate(LocalDate.of(1990, 1, 1))
                .intro("자기소개입니다")
                .profileImageUrl(null)
                .build();

        MemberResponse fakeResponse = MemberResponse.builder()
                .memberId(1L)
                .name("홍길동")
                .email("test@example.com")
                .nickname("길동이")
                .phoneNumber("010-1234-5678")
                .birthdate(LocalDate.of(1990, 1, 1))
                .coin(0)
                .intro("자기소개입니다")
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.jpg", "image/jpeg", "dummy image content".getBytes()
        );
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        MockMultipartFile json = new MockMultipartFile(
                "member", "member", "application/json",
                    objectMapper.writeValueAsBytes(request)

        );

        given(fileStorageService.store(any(), any())).willReturn("https://s3.com/profiles/uuid.jpg");
        given(memberService.register(any())).willReturn(fakeResponse);

        // when + then
        mockMvc.perform(multipart("/api/v1/members/register")
                        .file(file)
                        .file(json)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
    }
}
