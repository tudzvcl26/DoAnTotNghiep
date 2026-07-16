package com.recruitment.user.service;

import com.recruitment.user.dto.request.CreateCertificateRequest;
import com.recruitment.user.dto.request.UpdateCertificateRequest;
import com.recruitment.user.dto.response.CertificateResponse;
import com.recruitment.user.entity.Certificate;
import com.recruitment.user.entity.Profile;
import com.recruitment.user.exception.ResourceNotFoundException;
import com.recruitment.user.mapper.CertificateMapper;
import com.recruitment.user.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificateService {

    private final CertificateRepository repository;
    private final CertificateMapper mapper;
    private final ProfileService profileService;
    private final CompletionScoreService completionScoreService;

    @Transactional(readOnly = true)
    public Page<CertificateResponse> getAll(
            UUID userId,
            Pageable pageable
    ) {

        Profile profile = profileService.getByUserId(userId);

        return repository
                .findByProfileIdAndDeletedAtIsNull(
                        profile.getId(),
                        pageable
                )
                .map(mapper::toResponse);

    }

    @Transactional(readOnly = true)
    public CertificateResponse getById(
            UUID certificateId
    ) {

        Certificate entity = repository
                .findByIdAndDeletedAtIsNull(certificateId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Certificate not found"
                        ));

        return mapper.toResponse(entity);

    }

    public CertificateResponse create(
            UUID userId,
            CreateCertificateRequest request
    ) {

        Profile profile = profileService.getByUserId(userId);

        validateDate(
                request.getIssueDate(),
                request.getExpiryDate()
        );

        if (repository.existsByProfileIdAndCertificateNameAndIssuerNameAndIssueDateAndDeletedAtIsNull(
                profile.getId(),
                request.getCertificateName(),
                request.getIssuerName(),
                request.getIssueDate()
        )) {

            throw new IllegalArgumentException(
                    "Certificate already exists."
            );

        }

        Certificate entity = mapper.toEntity(request);

        entity.setProfile(profile);

        Certificate saved = repository.save(entity);

        completionScoreService.recalculate(profile);

        return mapper.toResponse(saved);

    }

    public CertificateResponse update(
            UUID certificateId,
            UpdateCertificateRequest request
    ) {

        Certificate entity = repository
                .findByIdAndDeletedAtIsNull(certificateId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Certificate not found"
                        ));

        validateDate(
                request.getIssueDate(),
                request.getExpiryDate()
        );

        mapper.updateEntity(
                request,
                entity
        );

        Certificate saved = repository.save(entity);

        completionScoreService.recalculate(saved.getProfile());

        return mapper.toResponse(saved);

    }

    public void delete(
            UUID certificateId
    ) {

        Certificate entity = repository
                .findByIdAndDeletedAtIsNull(certificateId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Certificate not found"
                        ));

        entity.setDeletedAt(LocalDateTime.now());

        repository.save(entity);

        completionScoreService.recalculate(entity.getProfile());

    }

    private void validateDate(
            LocalDate issueDate,
            LocalDate expiryDate
    ) {

        if (issueDate == null) {
            throw new IllegalArgumentException(
                    "Issue date is required."
            );
        }

        if (expiryDate != null &&
                expiryDate.isBefore(issueDate)) {

            throw new IllegalArgumentException(
                    "Expiry date must be after or equal to issue date."
            );

        }

    }

}