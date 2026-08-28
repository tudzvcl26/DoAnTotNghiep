package com.recruitment.company.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;

    private String code;

    private String message;

    private T data;

    private String path;

    private LocalDateTime timestamp;

    private String traceId;

    public static <T> ApiResponse<T> success(T data) {

        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message("Request successful.")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

    }

    public static <T> ApiResponse<T> success(
            String message,
            T data
    ) {

        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

    }

    public static <T> ApiResponse<T> error(
            String code,
            String message,
            String path
    ) {

        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .traceId(MDC.get("correlationId"))
                .build();

    }

}
