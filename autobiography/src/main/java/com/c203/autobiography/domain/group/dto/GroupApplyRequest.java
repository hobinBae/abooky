package com.c203.autobiography.domain.group.dto;

import com.c203.autobiography.domain.group.entity.ApplyStatus;
import com.c203.autobiography.domain.group.entity.GroupApply;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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

    @Schema(description = "초대할 회원 이메일", example = "user@example.com")
    @NotNull(message="받는 사람 이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String receiverEmail;

    public GroupApply toEntity(Long groupId, Long receiverId) {
        return GroupApply.builder()
                .groupId(groupId)
                .receiverId(receiverId)
                .status(ApplyStatus.PENDING)
                .build();
    }
}
