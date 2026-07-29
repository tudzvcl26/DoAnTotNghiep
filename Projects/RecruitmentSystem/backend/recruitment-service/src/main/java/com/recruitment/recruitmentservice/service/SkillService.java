package com.recruitment.recruitmentservice.service;

import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.skill.CreateSkillRequest;
import com.recruitment.recruitmentservice.dto.skill.SkillResponse;
import com.recruitment.recruitmentservice.dto.skill.UpdateSkillRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SkillService {

    SkillResponse create(CreateSkillRequest request);

    SkillResponse update(
            UUID id,
            UpdateSkillRequest request
    );

    void delete(UUID id);

    SkillResponse getById(UUID id);

    PageResponse<SkillResponse> getAll(Pageable pageable);

    PageResponse<SkillResponse> search(
            String keyword,
            Pageable pageable
    );

}