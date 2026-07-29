package com.recruitment.recruitmentservice.service.impl;

import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.benefit.BenefitResponse;
import com.recruitment.recruitmentservice.dto.benefit.CreateBenefitRequest;
import com.recruitment.recruitmentservice.dto.benefit.UpdateBenefitRequest;
import com.recruitment.recruitmentservice.entity.Benefit;
import com.recruitment.recruitmentservice.exception.BusinessException;
import com.recruitment.recruitmentservice.exception.ErrorCode;
import com.recruitment.recruitmentservice.mapper.BenefitMapper;
import com.recruitment.recruitmentservice.repository.BenefitRepository;
import com.recruitment.recruitmentservice.service.BenefitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BenefitServiceImpl implements BenefitService {

    private final BenefitRepository benefitRepository;

    private final BenefitMapper benefitMapper;

    @Override
    public BenefitResponse create(CreateBenefitRequest request) {

        validateBenefitName(request.getName());
        validateBenefitSlug(request.getSlug());

        Benefit entity = benefitMapper.toEntity(request);

        Benefit savedBenefit = benefitRepository.save(entity);

        return benefitMapper.toResponse(savedBenefit);
    }

    @Override
    public BenefitResponse update(
            UUID id,
            UpdateBenefitRequest request
    ) {

        Benefit benefit = getBenefitById(id);

        validateBenefitNameForUpdate(
                id,
                request.getName()
        );

        benefitMapper.updateEntity(
                request,
                benefit
        );

        Benefit updatedBenefit =
                benefitRepository.save(benefit);

        return benefitMapper.toResponse(updatedBenefit);
    }
    @Override
    public void delete(UUID id) {

        Benefit benefit = getBenefitById(id);

        benefit.setActive(false);

        benefitRepository.save(benefit);
    }

    @Override
    @Transactional(readOnly = true)
    public BenefitResponse getById(UUID id) {

        return benefitMapper.toResponse(
                getBenefitById(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BenefitResponse> getAll(
            Pageable pageable
    ) {

        return PageResponse.from(
                benefitRepository.findByActiveTrue(pageable),
                benefitMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BenefitResponse> search(
            String keyword,
            Pageable pageable
    ) {

        if (keyword == null || keyword.isBlank()) {
            return getAll(pageable);
        }

        return PageResponse.from(
                benefitRepository.findByActiveTrueAndNameContainingIgnoreCase(
                        keyword.trim(),
                        pageable
                ),
                benefitMapper::toResponse
        );
    }
    private Benefit getBenefitById(UUID id) {

        return benefitRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.BENEFIT_NOT_FOUND
                        ));
    }

    private void validateBenefitName(String name) {

        if (benefitRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException(
                    ErrorCode.BENEFIT_NAME_ALREADY_EXISTS
            );
        }
    }

    private void validateBenefitSlug(String slug) {

        if (benefitRepository.existsBySlug(slug)) {
            throw new BusinessException(
                    ErrorCode.BENEFIT_SLUG_ALREADY_EXISTS
            );
        }
    }

    private void validateBenefitNameForUpdate(
            UUID id,
            String name
    ) {

        if (benefitRepository.existsByNameIgnoreCaseAndIdNot(
                name,
                id
        )) {

            throw new BusinessException(
                    ErrorCode.BENEFIT_NAME_ALREADY_EXISTS
            );

        }

    }

}
