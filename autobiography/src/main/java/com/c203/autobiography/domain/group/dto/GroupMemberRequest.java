package com.c203.autobiography.domain.group.dto;

import com.c203.autobiography.domain.group.entity.GroupMember;
import com.c203.autobiography.domain.group.entity.GroupRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMemberRequest {

    @Schema(description = "추가할 멤버 ID", example = "1")
    @NotNull(message = "회원 ID는 필수 입니다.")
    private Long memberId;

    @Schema(description = "그룹 내 등급", example = "MEMBER")
    private GroupRole role = GroupRole.MEMBER;

    public GroupMember toEntity(Long groupId){
        return GroupMember.builder()
                .groupId(groupId)
                .memberId(this.memberId)
                .role(this.role)
                .build();
    }
}
