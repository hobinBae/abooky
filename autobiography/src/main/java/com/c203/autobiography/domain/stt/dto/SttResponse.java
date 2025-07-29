package com.c203.autobiography.domain.stt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SttResponse {
    private String text;
    private Usage usage;  // 추가된 필드

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private String type;
        private long seconds;
    }
}
