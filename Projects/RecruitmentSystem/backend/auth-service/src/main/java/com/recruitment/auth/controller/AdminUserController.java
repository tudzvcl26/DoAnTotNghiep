package com.recruitment.auth.controller;

import com.recruitment.auth.common.ApiResponse;
import com.recruitment.auth.dto.request.AdminUpdateUserEnabledRequest;
import com.recruitment.auth.dto.request.AdminUpdateUserRolesRequest;
import com.recruitment.auth.dto.response.AdminUserResponse;
import com.recruitment.auth.service.AdminUserService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<Page<AdminUserResponse>> getUsers(@RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) String role,
                                                         @RequestParam(required = false) Boolean enabled,
                                                         Pageable pageable, HttpServletRequest request) {
        return ApiResponse.success("Users retrieved successfully", adminUserService.getUsers(keyword, role, enabled, pageable), request.getRequestURI());
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserResponse> getUser(@PathVariable UUID id, HttpServletRequest request) {
        return ApiResponse.success("User retrieved successfully", adminUserService.getUser(id), request.getRequestURI());
    }

    @PatchMapping("/{id}/roles")
    public ApiResponse<AdminUserResponse> updateRoles(@PathVariable UUID id, @Valid @RequestBody AdminUpdateUserRolesRequest body, HttpServletRequest request) {
        return ApiResponse.success("User roles updated successfully", adminUserService.updateRoles(id, body.getRoles()), request.getRequestURI());
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<AdminUserResponse> updateEnabled(@PathVariable UUID id, @Valid @RequestBody AdminUpdateUserEnabledRequest body, HttpServletRequest request) {
        return ApiResponse.success("User status updated successfully", adminUserService.updateEnabled(id, body.getEnabled()), request.getRequestURI());
    }
}
