package com.c203.autobiography.domain.member.dto;

import com.c203.autobiography.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberCreateRequest {

    @Schema(description = "회원 이메일", example = "user@example.com")
    @NotBlank(message = "이메은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String email;

    @Schema(description = "비밀번호 (8~20자)", example = "securePass123!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8 ~ 20자 사이여야 합니다.")
    private String password;

    @Schema(description = "실명", example = "배싸피")
    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 최대 50자까지 가능합니다.")
    private String name;

    @Schema(description = "닉네임", example = "배싸피123")
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 50, message = "닉네임은 최대 50자까지 가능합니다.")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/uuid.jpg")
    @URL(message = "유효한 URL 형식이어야 합니다.")
    private String profileImageUrl;

    @Schema(description = "전화번호", example = "010-1234-5678")
//    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]-\\d{3,4}-\\d{4}$", message = "유효한 전화번호 형식이어야 합니다. 예: 010-1234-5678")
    private String phoneNumber;

    @Schema(description = "생년월일", example = "1999-06-23")
    @NotNull(message = "생년월일은 필수입니다.")
    private java.time.LocalDate birthdate;

    @Schema(description = "자기소개", example = "안녕하세요. 만나서 반가워요")
    @Size(max = 255, message = "자기소개는 최대 255자까지 가능합니다.")
    private String intro;

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .phoneNumber(phoneNumber)
                .birthdate(birthdate)
                .intro(intro)
                .coin(0)
                .role(Role.MEMBER)
                .build();
    }
}
