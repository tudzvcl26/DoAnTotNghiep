package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import com.recruitment.user.dto.request.UpdateProfileRequest;
import com.recruitment.user.dto.response.ProfileResponse;
import com.recruitment.user.security.CurrentUserId;
import com.recruitment.user.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMyProfile(
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Profile retrieved successfully",
                profileService.getProfile(CurrentUserId.get()),
                request.getRequestURI()
        );

    }

    @GetMapping
    public ApiResponse<Page<ProfileResponse>> searchProfiles(
            Pageable pageable,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Profiles retrieved successfully",
                profileService.search(pageable),
                request.getRequestURI()
        );

    }

    @PutMapping("/me")
    public ApiResponse<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Profile updated successfully",
                profileService.update(
                        CurrentUserId.get(),
                        body
                ),
                request.getRequestURI()
        );

    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteProfile(
            HttpServletRequest request
    ) {

        profileService.delete(CurrentUserId.get());

        return ApiResponse.success(
                "Profile deleted successfully",
                request.getRequestURI()
        );

    }

}