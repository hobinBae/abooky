package com.c203.autobiography.domain.group.dto;

import com.c203.autobiography.domain.group.entity.ApplyStatus;
import com.c203.autobiography.domain.group.entity.GroupApply;
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
public class GroupApplyRequest {

    @Schema(description = "초대할 회원 ID", example = "42")
    @NotNull(message="받는 사람 아이디는 필수입니다.")
    private Long receiverId;

    public GroupApply toEntity(Long groupId) {
        return GroupApply.builder()
                .groupId(groupId)
                .receiverId(this.receiverId)
                .status(ApplyStatus.PENDING)
                .build();
    }
}
