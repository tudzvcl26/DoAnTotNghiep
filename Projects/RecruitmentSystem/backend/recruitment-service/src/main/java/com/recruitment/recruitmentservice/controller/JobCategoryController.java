package com.recruitment.recruitmentservice.controller;

import com.recruitment.recruitmentservice.common.ApiResponse;
import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.category.CreateJobCategoryRequest;
import com.recruitment.recruitmentservice.dto.category.JobCategoryResponse;
import com.recruitment.recruitmentservice.dto.category.UpdateJobCategoryRequest;
import com.recruitment.recruitmentservice.exception.BusinessException;
import com.recruitment.recruitmentservice.exception.ErrorCode;
import com.recruitment.recruitmentservice.service.JobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/job-categories")
@RequiredArgsConstructor
@Tag(
        name = "Job Category",
        description = "APIs for managing job categories"
)
public class JobCategoryController {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> SORT_FIELDS = Set.of(
            "name",
            "displayOrder",
            "createdAt",
            "updatedAt"
    );

    private final JobCategoryService jobCategoryService;

    @Operation(summary = "Create a new job category")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<JobCategoryResponse> create(
            @Valid @RequestBody CreateJobCategoryRequest request
    ) {

        return ApiResponse.success(
                "Job category created successfully.",
                jobCategoryService.create(request)
        );
    }

    @Operation(summary = "Update a job category")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<JobCategoryResponse> update(
            @Parameter(description = "Job Category ID")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJobCategoryRequest request
    ) {

        return ApiResponse.success(
                "Job category updated successfully.",
                jobCategoryService.update(id, request)
        );
    }

    @Operation(summary = "Delete a job category")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(
            @Parameter(description = "Job Category ID")
            @PathVariable UUID id
    ) {

        jobCategoryService.delete(id);

        return ApiResponse.success(
                "Job category deleted successfully.",
                null
        );
    }

    @Operation(summary = "Get job category by ID")
    @GetMapping("/{id}")
    public ApiResponse<JobCategoryResponse> getById(
            @Parameter(description = "Job Category ID")
            @PathVariable UUID id
    ) {

        return ApiResponse.success(
                jobCategoryService.getById(id)
        );
    }

    @Operation(summary = "Get all job categories")
    @GetMapping
    public ApiResponse<PageResponse<JobCategoryResponse>> getAll(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "displayOrder")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction
    ) {

        Pageable pageable = createPageable(
                page,
                size,
                sortBy,
                direction
        );

        return ApiResponse.success(
                jobCategoryService.getAll(pageable)
        );
    }

    @Operation(summary = "Search job categories")
    @GetMapping("/search")
    public ApiResponse<PageResponse<JobCategoryResponse>> search(

            @RequestParam
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "displayOrder")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction
    ) {

        Pageable pageable = createPageable(
                page,
                size,
                sortBy,
                direction
        );

        return ApiResponse.success(
                jobCategoryService.search(
                        keyword,
                        pageable
                )
        );
    }

    private Pageable createPageable(
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        if (page < 0
                || size < 1
                || size > MAX_PAGE_SIZE
                || !SORT_FIELDS.contains(sortBy)
                || !("asc".equalsIgnoreCase(direction)
                || "desc".equalsIgnoreCase(direction))) {

            throw new BusinessException(
                    ErrorCode.INVALID_PAGINATION_OR_SORT
            );
        }

        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return PageRequest.of(page, size, sort);
    }

}
