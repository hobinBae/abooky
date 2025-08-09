package com.c203.autobiography.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberUpdateRequest {

    @Schema(description = "변경할 닉네임", example = "싸피123")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하로 입력해주세요.")
    private String nickname;

    @Schema(description = "전화번호", example = "010-1234-5678")
    @Pattern(regexp = "^01[0-9]-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678")
    private String phoneNumber;

    @Schema(description = "프로필 이미지 URL", example = "https://...jpg")
    @URL(message = "유효한 URL 형식이어야 합니다.")
    private String profileImageUrl;

    @Schema(description = "자기소개", example = "안녕하세요. 만나서 반가워요")
    @Size(max = 255, message = "자기소개는 최대 255자까지 입력 가능합니다.")
    private String intro;

}
