package com.c203.autobiography.domain.group.dto;

import com.c203.autobiography.domain.group.entity.GroupMember;
import com.c203.autobiography.domain.group.entity.GroupRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupMemberResponse {

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "회원 역할", example = "MEMBER")
    private GroupRole role;

    public static GroupMemberResponse from(GroupMember gm){
        return GroupMemberResponse.builder()
                .groupId(gm.getGroupId())
                .memberId(gm.getMemberId())
                .role(gm.getRole())
                .build();
    }

}
