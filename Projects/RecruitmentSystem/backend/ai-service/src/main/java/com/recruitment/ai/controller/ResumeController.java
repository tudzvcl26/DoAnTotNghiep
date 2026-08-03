package com.recruitment.ai.controller;

import com.recruitment.ai.common.ApiResponse;
import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.dto.response.ResumeAnalysisResponse;
import com.recruitment.ai.dto.response.ResumeDocumentResponse;
import com.recruitment.ai.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume Analysis", description = "Private resume upload and deterministic quality analysis APIs")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Upload and extract a candidate resume")
    public ApiResponse<ResumeDocumentResponse> upload(
            @Parameter(description = "PDF, DOCX, or UTF-8 TXT resume; maximum 10 MB")
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.success("Resume uploaded successfully.", resumeService.upload(file));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    @Operation(summary = "List resumes visible to the current user")
    public ApiResponse<PageResponse<ResumeDocumentResponse>> getResumes(@ParameterObject Pageable pageable) {
        return ApiResponse.success(resumeService.getResumes(pageable));
    }

    @GetMapping("/{resumeId}")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    @Operation(summary = "Get private resume metadata")
    public ApiResponse<ResumeDocumentResponse> getResume(@PathVariable UUID resumeId) {
        return ApiResponse.success(resumeService.getResume(resumeId));
    }

    @PostMapping("/{resumeId}/analyze")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    @Operation(summary = "Extract structured facts and calculate deterministic resume quality score")
    public ApiResponse<ResumeAnalysisResponse> analyze(@PathVariable UUID resumeId) {
        return ApiResponse.success("Resume analyzed successfully.", resumeService.analyze(resumeId));
    }

    @GetMapping("/{resumeId}/analysis")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    @Operation(summary = "Get the persisted resume analysis")
    public ApiResponse<ResumeAnalysisResponse> getAnalysis(@PathVariable UUID resumeId) {
        return ApiResponse.success(resumeService.getAnalysis(resumeId));
    }

    @DeleteMapping("/{resumeId}")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    @Operation(summary = "Delete a resume, its private object, and analysis")
    public ApiResponse<Void> delete(@PathVariable UUID resumeId) {
        resumeService.delete(resumeId);
        return ApiResponse.success("Resume deleted successfully.", null);
    }
}
