package com.recruitment.ai.service;

import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.dto.response.ResumeAnalysisResponse;
import com.recruitment.ai.dto.response.ResumeDocumentResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ResumeService {

    ResumeDocumentResponse upload(MultipartFile file);

    PageResponse<ResumeDocumentResponse> getResumes(Pageable pageable);

    ResumeDocumentResponse getResume(UUID resumeId);

    ResumeAnalysisResponse analyze(UUID resumeId);

    ResumeAnalysisResponse getAnalysis(UUID resumeId);

    void delete(UUID resumeId);
}
