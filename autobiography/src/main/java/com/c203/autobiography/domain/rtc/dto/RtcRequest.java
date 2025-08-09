package com.c203.autobiography.domain.rtc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RtcRequest {

    @NotBlank(message = "userName은 필수입니다.")
    private String userName;
}
