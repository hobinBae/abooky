package com.c203.autobiography.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepresentBookResponse{
    private Long memberId;
    private Long representBookId; // null이면 해제 상태
}
