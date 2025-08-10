package com.c203.autobiography.domain.group.dto;

import com.c203.autobiography.domain.group.entity.ApplyStatus;
import com.c203.autobiography.domain.group.entity.Group;
import com.c203.autobiography.domain.group.entity.GroupApply;
import com.c203.autobiography.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupApplyResponse {

    @Schema(description = "그룹 초대 고유 아이디", example = "1")
    private Long groupApplyId;

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "그룹 이름", example = "1")
    private String groupName;

    @Schema(description = "그룹 방장 ID", example = "1")
    private Long leaderId;

    @Schema(description = "그룹 방장 이름", example = "1")
    private String leaderNickname;

    @Schema(description = "초대 상태", example = "PENDING")
    private ApplyStatus status;

    @Schema(description = "초대 생성 일시", example = "2025-07-23T10:00:00")
    private LocalDateTime invitedAt;

    public static GroupApplyResponse from(GroupApply ga, Group g, Member leader) {
        return GroupApplyResponse.builder()
                .groupApplyId(ga.getGroupApplyId())
                .groupId(ga.getGroupId())
                .groupName(g.getGroupName())
                .leaderId(leader.getMemberId())
                .leaderNickname(leader.getNickname())
                .status(ga.getStatus())
                .invitedAt(ga.getInvitedAt())
                .build();
    }
}
