package com.recruitment.company.controller;

import com.recruitment.company.dto.request.CreateCompanyRequest;
import com.recruitment.company.dto.request.UpdateCompanyRequest;
import com.recruitment.company.dto.response.CompanyResponse;
import com.recruitment.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/companies")
@Tag(
        name = "Company",
        description = "Company Management API"
)
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @Operation(
            summary = "Create company",
            description = "Create a new company."
    )
    public ResponseEntity<CompanyResponse> create(

            @Valid
            @RequestBody
            CreateCompanyRequest request

    ) {

        CompanyResponse response =
                companyService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }

    @PutMapping("/{companyId}")
    @Operation(
            summary = "Update company",
            description = "Update company information."
    )
    public ResponseEntity<CompanyResponse> update(

            @PathVariable
            UUID companyId,

            @Valid
            @RequestBody
            UpdateCompanyRequest request

    ) {

        CompanyResponse response =
                companyService.update(
                        companyId,
                        request
                );

        return ResponseEntity.ok(response);

    }

    @GetMapping("/{companyId}")
    @Operation(
            summary = "Get company by id",
            description = "Retrieve company information by company id."
    )
    public ResponseEntity<CompanyResponse> getById(

            @PathVariable
            UUID companyId

    ) {

        return ResponseEntity.ok(

                companyService.getById(companyId)

        );

    }

    @GetMapping("/slug/{slug}")
    @Operation(
            summary = "Get company by slug",
            description = "Retrieve company information by slug."
    )
    public ResponseEntity<CompanyResponse> getBySlug(

            @PathVariable
            String slug

    ) {

        return ResponseEntity.ok(

                companyService.getBySlug(slug)

        );

    }

    @GetMapping
    @Operation(
            summary = "Get all companies",
            description = "Retrieve all active companies with pagination."
    )
    public ResponseEntity<Page<CompanyResponse>> getAll(

            Pageable pageable

    ) {

        return ResponseEntity.ok(

                companyService.getAll(pageable)

        );

    }

    @GetMapping("/search")
    @Operation(
            summary = "Search companies",
            description = "Search companies by keyword."
    )
    public ResponseEntity<Page<CompanyResponse>> search(

            @RequestParam(required = false)
            String keyword,

            Pageable pageable

    ) {

        return ResponseEntity.ok(

                companyService.search(
                        keyword,
                        pageable
                )

        );

    }

    @DeleteMapping("/{companyId}")
    @Operation(
            summary = "Delete company",
            description = "Soft delete a company."
    )
    public ResponseEntity<Void> delete(

            @PathVariable
            UUID companyId

    ) {

        companyService.delete(companyId);

        return ResponseEntity.noContent().build();

    }

}