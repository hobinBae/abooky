package com.c203.autobiography.domain.group.dto;

import com.c203.autobiography.domain.group.entity.ApplyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupApplyStatusRequest {

    @Schema(description = "처리할 초대 상태", example = "PENDING", allowableValues = {"PENDING", "ACCEPTED", "DENIED"})
    @NotNull(message = "상태는 필수입니다.")
    private ApplyStatus status;
}
