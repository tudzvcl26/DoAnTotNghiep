package com.recruitment.recruitmentservice.controller;

import com.recruitment.recruitmentservice.common.ApiResponse;
import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.skill.CreateSkillRequest;
import com.recruitment.recruitmentservice.dto.skill.SkillResponse;
import com.recruitment.recruitmentservice.dto.skill.UpdateSkillRequest;
import com.recruitment.recruitmentservice.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Skill",
        description = "Skill management APIs"
)
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new skill")
    public ApiResponse<SkillResponse> create(

            @Valid
            @RequestBody
            CreateSkillRequest request

    ) {

        return ApiResponse.success(
                "Skill created successfully.",
                skillService.create(request)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a skill")
    public ApiResponse<SkillResponse> update(

            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            UpdateSkillRequest request

    ) {

        return ApiResponse.success(
                "Skill updated successfully.",
                skillService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a skill")
    public ApiResponse<Void> delete(

            @PathVariable
            UUID id

    ) {

        skillService.delete(id);

        return ApiResponse.success(
                "Skill deleted successfully.",
                null
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill by id")
    public ApiResponse<SkillResponse> getById(

            @PathVariable
            UUID id

    ) {

        return ApiResponse.success(
                skillService.getById(id)
        );
    }

    @GetMapping
    @Operation(summary = "Get all skills")
    public ApiResponse<PageResponse<SkillResponse>> getAll(

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10")
            int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name")
            String sortBy,

            @Parameter(description = "Sort direction (asc|desc)")
            @RequestParam(defaultValue = "asc")
            String direction

    ) {

        Pageable pageable = buildPageable(
                page,
                size,
                sortBy,
                direction
        );

        return ApiResponse.success(
                skillService.getAll(pageable)
        );
    }

    @GetMapping("/search")
    @Operation(summary = "Search skills")
    public ApiResponse<PageResponse<SkillResponse>> search(

            @RequestParam(required = false)
            String keyword,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10")
            int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name")
            String sortBy,

            @Parameter(description = "Sort direction (asc|desc)")
            @RequestParam(defaultValue = "asc")
            String direction

    ) {

        Pageable pageable = buildPageable(
                page,
                size,
                sortBy,
                direction
        );

        return ApiResponse.success(
                skillService.search(
                        keyword,
                        pageable
                )
        );
    }

    private Pageable buildPageable(
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 10;
        }

        if (size > 100) {
            size = 100;
        }

        Sort.Direction sortDirection =
                "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        return PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );
    }

}