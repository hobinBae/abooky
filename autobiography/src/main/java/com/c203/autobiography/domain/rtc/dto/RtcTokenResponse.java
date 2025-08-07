package com.c203.autobiography.domain.rtc.dto;

import lombok.*;

@Getter
@AllArgsConstructor
public class RtcTokenResponse {
    private String url; // ws:// or wss://...
    private String token;
}
