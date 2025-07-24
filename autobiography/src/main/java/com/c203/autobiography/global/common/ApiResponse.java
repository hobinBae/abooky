package com.c203.autobiography.global.common;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private int status;
    private String message;
    private T data;
    private String timestamp;
    private String path;

    /**
     * 성공 응답 팩토리 메서드
     */
    public static <T> ApiResponse<T> of(
            HttpStatus status,
            String message,
            T data,
            String requestPath
    ) {
        return new ApiResponse<>(
                true,
                status.value(),
                message,
                data,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                requestPath
        );
    }
}
