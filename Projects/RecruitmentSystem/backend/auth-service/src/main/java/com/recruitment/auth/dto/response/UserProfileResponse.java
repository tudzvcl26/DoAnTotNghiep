package com.recruitment.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UserProfileResponse {

    private UUID id;

    private String email;

    private String fullName;

    private String phone;

    private String avatarUrl;

    private Boolean enabled;

    private Boolean verified;

    private List<String> roles;

}