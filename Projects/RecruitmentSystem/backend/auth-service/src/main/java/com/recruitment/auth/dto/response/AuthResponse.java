package com.recruitment.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiresIn;

}