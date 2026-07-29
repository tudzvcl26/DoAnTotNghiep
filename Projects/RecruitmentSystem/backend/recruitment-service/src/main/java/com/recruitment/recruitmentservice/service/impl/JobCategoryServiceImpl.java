package com.recruitment.recruitmentservice.service.impl;

import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.category.CreateJobCategoryRequest;
import com.recruitment.recruitmentservice.dto.category.JobCategoryResponse;
import com.recruitment.recruitmentservice.dto.category.UpdateJobCategoryRequest;
import com.recruitment.recruitmentservice.entity.JobCategory;
import com.recruitment.recruitmentservice.exception.BusinessException;
import com.recruitment.recruitmentservice.exception.ErrorCode;
import com.recruitment.recruitmentservice.mapper.JobCategoryMapper;
import com.recruitment.recruitmentservice.repository.JobCategoryRepository;
import com.recruitment.recruitmentservice.repository.JobRepository;
import com.recruitment.recruitmentservice.service.JobCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class JobCategoryServiceImpl implements JobCategoryService {

    private final JobCategoryRepository jobCategoryRepository;

    private final JobRepository jobRepository;

    private final JobCategoryMapper jobCategoryMapper;

    @Override
    public JobCategoryResponse create(CreateJobCategoryRequest request) {

        validateCategoryName(request.getName());
        validateCategorySlug(request.getSlug());

        JobCategory entity = jobCategoryMapper.toEntity(request);

        if (request.getParentId() != null) {

            JobCategory parentCategory =
                    getJobCategoryById(request.getParentId());

            entity.setParent(parentCategory);
        }

        JobCategory savedCategory =
                jobCategoryRepository.save(entity);

        return jobCategoryMapper.toResponse(savedCategory);
    }

    @Override
    public JobCategoryResponse update(
            UUID id,
            UpdateJobCategoryRequest request
    ) {

        JobCategory category =
                getJobCategoryById(id);

        validateCategoryNameForUpdate(
                id,
                request.getName()
        );

        jobCategoryMapper.updateEntity(
                request,
                category
        );

        if (request.getParentId() == null) {

            category.setParent(null);

        } else {

            if (id.equals(request.getParentId())) {
                throw new BusinessException(
                        ErrorCode.INVALID_PARENT_CATEGORY
                );
            }

            JobCategory parentCategory =
                    getJobCategoryById(request.getParentId());

            validateParentCategory(id, parentCategory);

            category.setParent(parentCategory);
        }

        JobCategory updatedCategory =
                jobCategoryRepository.save(category);

        return jobCategoryMapper.toResponse(updatedCategory);
    }
    @Override
    public void delete(UUID id) {

        if (jobCategoryRepository.existsByParent_Id(id)) {
            throw new BusinessException(
                    ErrorCode.JOB_CATEGORY_HAS_CHILDREN
            );
        }

        if (jobRepository.existsByCategory_Id(id)) {
            throw new BusinessException(
                    ErrorCode.JOB_CATEGORY_IN_USE
            );
        }

        JobCategory category = getJobCategoryById(id);

        category.setActive(false);

        jobCategoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public JobCategoryResponse getById(UUID id) {

        return jobCategoryMapper.toResponse(
                getJobCategoryById(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobCategoryResponse> getAll(
            Pageable pageable
    ) {

        return PageResponse.from(
                jobCategoryRepository.findByActiveTrue(pageable),
                jobCategoryMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobCategoryResponse> search(
            String keyword,
            Pageable pageable
    ) {

        if (keyword == null || keyword.isBlank()) {
            return getAll(pageable);
        }

        return PageResponse.from(
                jobCategoryRepository.findByActiveTrueAndNameContainingIgnoreCase(
                        keyword.trim(),
                        pageable
                ),
                jobCategoryMapper::toResponse
        );
    }

    private JobCategory getJobCategoryById(UUID id) {

        return jobCategoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.JOB_CATEGORY_NOT_FOUND
                        ));
    }

    private void validateCategorySlug(String slug) {

        if (jobCategoryRepository.existsBySlug(slug)) {
            throw new BusinessException(
                    ErrorCode.JOB_CATEGORY_SLUG_ALREADY_EXISTS
            );
        }
    }

    private void validateParentCategory(
            UUID categoryId,
            JobCategory parentCategory
    ) {

        JobCategory currentCategory = parentCategory;

        while (currentCategory != null) {

            if (categoryId.equals(currentCategory.getId())) {
                throw new BusinessException(
                        ErrorCode.INVALID_PARENT_CATEGORY
                );
            }

            currentCategory = currentCategory.getParent();
        }

    }

    private void validateCategoryName(String name) {

        if (jobCategoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException(
                    ErrorCode.JOB_CATEGORY_NAME_ALREADY_EXISTS
            );
        }
    }

    private void validateCategoryNameForUpdate(
            UUID id,
            String name
    ) {

        if (jobCategoryRepository.existsByNameIgnoreCaseAndIdNot(
                name,
                id
        )) {

            throw new BusinessException(
                    ErrorCode.JOB_CATEGORY_NAME_ALREADY_EXISTS
            );

        }

    }
}
