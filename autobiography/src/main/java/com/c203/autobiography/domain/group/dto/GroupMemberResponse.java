package com.c203.autobiography.domain.group.dto;

import com.c203.autobiography.domain.group.entity.GroupMember;
import com.c203.autobiography.domain.group.entity.GroupRole;
import com.c203.autobiography.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupMemberResponse {

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "회원 이름", example = "김싸피")
    private String name;

    @Schema(description = "회원 닉네임", example = "싸피123")
    private String nickname;

    @Schema(description = "회원 프로필 이미지 URL", example = "https://.../.jpg")
    private String profileImageUrl;

    @Schema(description = "회원 역할", example = "MEMBER")
    private GroupRole role;

    @Schema(description = "가입 시각", example = "2025-07-23T10:00:00")
    private LocalDateTime joinedAt;

    @Schema(description = "탈퇴(강퇴) 시각", example = "2025-07-23T10:00:00")
    private LocalDateTime deletedAt;

    public static GroupMemberResponse from(GroupMember gm, Member member){
        return GroupMemberResponse.builder()
                .groupId(gm.getGroupId())
                .memberId(gm.getMemberId())
                .name(member.getName())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .role(gm.getRole())
                .joinedAt(gm.getJoinedAt())
                .build();
    }

}
