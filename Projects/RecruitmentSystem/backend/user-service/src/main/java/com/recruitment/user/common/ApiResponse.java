package com.recruitment.user.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;

    private final String code;

    private final String message;

    private final T data;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    private final String path;

    public static <T> ApiResponse<T> success(
            String message,
            T data,
            String path
    ) {

        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .data(data)
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> success(
            String message,
            String path
    ) {

        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .path(path)
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
                .build();
    }

}
