package com.recruitment.recruitmentservice.service;

import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.category.CreateJobCategoryRequest;
import com.recruitment.recruitmentservice.dto.category.JobCategoryResponse;
import com.recruitment.recruitmentservice.dto.category.UpdateJobCategoryRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JobCategoryService {

    JobCategoryResponse create(CreateJobCategoryRequest request);

    JobCategoryResponse update(
            UUID id,
            UpdateJobCategoryRequest request
    );

    void delete(UUID id);

    JobCategoryResponse getById(UUID id);

    PageResponse<JobCategoryResponse> getAll(Pageable pageable);

    PageResponse<JobCategoryResponse> search(
            String keyword,
            Pageable pageable
    );

}