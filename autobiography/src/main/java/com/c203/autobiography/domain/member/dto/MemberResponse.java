package com.c203.autobiography.domain.member.dto;

import com.c203.autobiography.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {

    @Schema(description = "회원 고유 ID", example = "1")
    private Long memberId;

    @Schema(description = "회원 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "배싸피")
    private String name;

    @Schema(description = "닉네임", example = "배싸피123")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://...jpg")
    private String profileImageUrl;

    @Schema(description = "전화번호", example = "010-3322-1111")
    private String phoneNumber;

    @Schema(description = "생년월일", example = "1999-01-01")
    private LocalDate birthdate;

    @Schema(description = "보유 코인", example = "100")
    private int coin;

    @Schema(description = "자기소개", example = "안녕하세요. 만나서 반가워요")
    private String intro;

    @Schema(description = "대표 자서전 ID", example = "42", nullable = true)
    private Long representBookId;

    @Schema(description = "회원 가입 일시", example = "2025-07-23T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "회원 정보 마지막 수정일", example = "2025-07-23T10:30:00")
    private LocalDateTime updatedAt;

    public static MemberResponse from(Member m) {
        return MemberResponse.builder()
                .memberId(m.getMemberId())
                .name(m.getName())
                .email(m.getEmail())
                .nickname(m.getNickname())
                .profileImageUrl(m.getProfileImageUrl())
                .phoneNumber(m.getPhoneNumber())
                .birthdate(m.getBirthdate())
                .coin(m.getCoin())
                .intro(m.getIntro())
                .representBookId(m.getRepresentBookId())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
