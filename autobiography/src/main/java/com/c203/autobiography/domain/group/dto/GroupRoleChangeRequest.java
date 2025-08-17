package com.c203.autobiography.domain.group.dto;

import com.c203.autobiography.domain.group.entity.GroupRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupRoleChangeRequest {

    @Schema(description = "변경할 역할", example = "MANAGER")
    @NotNull(message = "변경할 역할을 선택해주세요")
    private GroupRole role;

}