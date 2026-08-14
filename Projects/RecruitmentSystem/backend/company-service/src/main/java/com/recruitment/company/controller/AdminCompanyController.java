package com.recruitment.company.controller;

import com.recruitment.company.dto.request.UpdateCompanyVerificationRequest;
import com.recruitment.company.dto.response.CompanyResponse;
import com.recruitment.company.enums.CompanyStatus;
import com.recruitment.company.enums.VerificationStatus;
import com.recruitment.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCompanyController {
    private final CompanyService companyService;

    @GetMapping
    public Page<CompanyResponse> getCompanies(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) CompanyStatus status,
                                              @RequestParam(required = false) VerificationStatus verificationStatus,
                                              @RequestParam(required = false) UUID ownerId,
                                              Pageable pageable) {
        return companyService.getAdminCompanies(keyword, status, verificationStatus, ownerId, pageable);
    }

    @GetMapping("/{id}")
    public CompanyResponse getCompany(@PathVariable UUID id) {
        return companyService.getAdminCompany(id);
    }

    @PatchMapping("/{id}/verification")
    public CompanyResponse updateVerification(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateCompanyVerificationRequest request) {
        return companyService.updateVerification(id, request.getVerificationStatus());
    }
}
