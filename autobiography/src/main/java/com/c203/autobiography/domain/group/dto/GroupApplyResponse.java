package com.c203.autobiography.domain.group.dto;

import com.c203.autobiography.domain.group.entity.ApplyStatus;
import com.c203.autobiography.domain.group.entity.GroupApply;
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

    @Schema(description = "초대받은 사용자 ID", example = "1")
    private Long receiverId;

    @Schema(description = "초대 상태", example = "PENDING")
    private ApplyStatus status;

    @Schema(description = "초대 생성 일시", example = "2025-07-23T10:00:00")
    private LocalDateTime invitedAt;

    public static GroupApplyResponse from(GroupApply ga) {
        return GroupApplyResponse.builder()
                .groupApplyId(ga.getGroupId())
                .groupId(ga.getGroupId())
                .receiverId(ga.getReceiverId())
                .status(ga.getStatus())
                .invitedAt(LocalDateTime.now())
                .build();
    }
}
