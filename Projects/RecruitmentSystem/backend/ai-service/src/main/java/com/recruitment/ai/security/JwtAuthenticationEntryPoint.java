package com.recruitment.ai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.dto.response.AiErrorResponse;
import com.recruitment.ai.exception.ErrorCode;
import com.recruitment.ai.util.CorrelationIds;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException {
        response.setStatus(ErrorCode.UNAUTHORIZED.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), AiErrorResponse.builder()
                .success(false)
                .code(ErrorCode.UNAUTHORIZED.getCode())
                .message(ErrorCode.UNAUTHORIZED.getMessage())
                .retryable(false)
                .correlationId(CorrelationIds.current(request))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

}
