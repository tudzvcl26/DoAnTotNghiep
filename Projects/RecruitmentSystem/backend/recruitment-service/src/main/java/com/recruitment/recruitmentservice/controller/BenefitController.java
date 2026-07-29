package com.recruitment.recruitmentservice.controller;

import com.recruitment.recruitmentservice.common.ApiResponse;
import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.benefit.BenefitResponse;
import com.recruitment.recruitmentservice.dto.benefit.CreateBenefitRequest;
import com.recruitment.recruitmentservice.dto.benefit.UpdateBenefitRequest;
import com.recruitment.recruitmentservice.service.BenefitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/benefits")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Benefit",
        description = "Benefit management APIs"
)
public class BenefitController {

    private final BenefitService benefitService;

    @PostMapping
    @Operation(summary = "Create a new benefit")
    public ApiResponse<BenefitResponse> create(

            @Valid
            @RequestBody
            CreateBenefitRequest request

    ) {

        return ApiResponse.success(
                "Benefit created successfully.",
                benefitService.create(request)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a benefit")
    public ApiResponse<BenefitResponse> update(

            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            UpdateBenefitRequest request

    ) {

        return ApiResponse.success(
                "Benefit updated successfully.",
                benefitService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a benefit")
    public ApiResponse<Void> delete(

            @PathVariable
            UUID id

    ) {

        benefitService.delete(id);

        return ApiResponse.success(
                "Benefit deleted successfully.",
                null
        );
    }
@GetMapping("/{id}")
@Operation(summary = "Get benefit by id")
public ApiResponse<BenefitResponse> getById(

        @PathVariable
        UUID id

) {

    return ApiResponse.success(
            benefitService.getById(id)
    );
}

@GetMapping
@Operation(summary = "Get all benefits")
public ApiResponse<PageResponse<BenefitResponse>> getAll(

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
            benefitService.getAll(pageable)
    );
}

@GetMapping("/search")
@Operation(summary = "Search benefits")
public ApiResponse<PageResponse<BenefitResponse>> search(

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
            benefitService.search(
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