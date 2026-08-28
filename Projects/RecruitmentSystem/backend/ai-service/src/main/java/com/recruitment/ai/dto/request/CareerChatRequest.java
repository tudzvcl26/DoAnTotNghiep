package com.recruitment.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CareerChatRequest(
        @NotBlank(message = "Vui lòng nhập câu hỏi cụ thể hơn để mình có thể hỗ trợ.")
        @Size(min = 3, max = 2000, message = "Câu hỏi phải có từ 3 đến 2000 ký tự.")
        String message,
        UUID resumeId,
        UUID jobId
) { }
