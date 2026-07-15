package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import com.recruitment.user.dto.request.CreateSkillRequest;
import com.recruitment.user.dto.request.UpdateSkillRequest;
import com.recruitment.user.dto.response.SkillResponse;
import com.recruitment.user.service.UserSkillService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/skills")
@RequiredArgsConstructor
public class UserSkillController {

    private final UserSkillService userSkillService;

    @GetMapping
    public ApiResponse<Page<SkillResponse>> getAll(
            @PathVariable UUID userId,
            Pageable pageable,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Skills retrieved successfully",
                userSkillService.getAll(userId, pageable),
                request.getRequestURI()
        );

    }

    @GetMapping("/{userSkillId}")
    public ApiResponse<SkillResponse> getById(
            @PathVariable UUID userSkillId,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Skill retrieved successfully",
                userSkillService.getById(userSkillId),
                request.getRequestURI()
        );

    }

    @PostMapping
    public ApiResponse<SkillResponse> create(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateSkillRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Skill created successfully",
                userSkillService.create(userId, body),
                request.getRequestURI()
        );

    }

    @PutMapping("/{userSkillId}")
    public ApiResponse<SkillResponse> update(
            @PathVariable UUID userSkillId,
            @Valid @RequestBody UpdateSkillRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Skill updated successfully",
                userSkillService.update(userSkillId, body),
                request.getRequestURI()
        );

    }

    @DeleteMapping("/{userSkillId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID userSkillId,
            HttpServletRequest request
    ) {

        userSkillService.delete(userSkillId);

        return ApiResponse.success(
                "Skill deleted successfully",
                request.getRequestURI()
        );

    }

}