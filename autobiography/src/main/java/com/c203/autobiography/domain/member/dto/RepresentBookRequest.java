package com.c203.autobiography.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RepresentBookRequest {
    @NotNull
    private Boolean isRepresentative; // true=설정, false=해제
}
