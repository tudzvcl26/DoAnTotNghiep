package com.recruitment.auth.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Thành công hay thất bại
     */
    private final boolean success;

    /**
     * Mã phản hồi
     * Ví dụ:
     * SUCCESS
     * VALIDATION_ERROR
     * UNAUTHORIZED
     */
    private final String code;

    /**
     * Thông điệp trả về
     */
    private final String message;

    /**
     * Dữ liệu trả về
     */
    private final T data;

    /**
     * Thời gian phản hồi
     */
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Đường dẫn API
     */
    private final String path;

    private final String traceId;

    /**
     * Response thành công có dữ liệu
     */
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

    /**
     * Response thành công không có dữ liệu
     */
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

    /**
     * Response thất bại
     */
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
                .traceId(MDC.get("correlationId"))
                .build();
    }

}
