package com.recruitment.recruitmentservice.service;

import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.benefit.BenefitResponse;
import com.recruitment.recruitmentservice.dto.benefit.CreateBenefitRequest;
import com.recruitment.recruitmentservice.dto.benefit.UpdateBenefitRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BenefitService {

    BenefitResponse create(CreateBenefitRequest request);

    BenefitResponse update(
            UUID id,
            UpdateBenefitRequest request
    );

    void delete(UUID id);

    BenefitResponse getById(UUID id);

    PageResponse<BenefitResponse> getAll(
            Pageable pageable
    );

    PageResponse<BenefitResponse> search(
            String keyword,
            Pageable pageable
    );

}