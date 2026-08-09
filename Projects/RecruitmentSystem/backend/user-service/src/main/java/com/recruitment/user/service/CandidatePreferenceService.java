package com.recruitment.user.service;

import com.recruitment.user.dto.request.CreateCandidatePreferenceRequest;
import com.recruitment.user.dto.request.UpdateCandidatePreferenceRequest;
import com.recruitment.user.dto.response.CandidatePreferenceResponse;
import com.recruitment.user.entity.CandidatePreference;
import com.recruitment.user.entity.Profile;
import com.recruitment.user.exception.ResourceNotFoundException;
import com.recruitment.user.mapper.CandidatePreferenceMapper;
import com.recruitment.user.repository.CandidatePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CandidatePreferenceService {

    private final CandidatePreferenceRepository repository;
    private final CandidatePreferenceMapper mapper;
    private final ProfileService profileService;
    private final CompletionScoreService completionScoreService;

    @Transactional(readOnly = true)
    public CandidatePreferenceResponse get(
            UUID userId
    ) {

        profileService.assertProfileOwner(userId);

        Profile profile = profileService.getByUserId(userId);

        CandidatePreference entity = repository
                .findByProfileIdAndDeletedAtIsNull(profile.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate preference not found"
                        ));

        return mapper.toResponse(entity);

    }

    public CandidatePreferenceResponse create(
            UUID userId,
            CreateCandidatePreferenceRequest request
    ) {

        profileService.assertProfileOwner(userId);

        Profile profile = profileService.getByUserId(userId);

        validateSalary(
                request.getSalaryMinimum(),
                request.getSalaryMaximum()
        );

        if (repository.findByProfileIdAndDeletedAtIsNull(profile.getId()).isPresent()) {

            throw new IllegalArgumentException(
                    "Candidate preference already exists."
            );

        }

        CandidatePreference entity = mapper.toEntity(request);

        entity.setProfile(profile);

        CandidatePreference saved = repository.save(entity);

        completionScoreService.recalculate(profile);

        return mapper.toResponse(saved);

    }

    public CandidatePreferenceResponse update(
            UUID userId,
            UpdateCandidatePreferenceRequest request
    ) {

        profileService.assertProfileOwner(userId);

        Profile profile = profileService.getByUserId(userId);

        CandidatePreference entity = repository
                .findByProfileIdAndDeletedAtIsNull(profile.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate preference not found"
                        ));

        validateSalary(
                request.getSalaryMinimum(),
                request.getSalaryMaximum()
        );

        mapper.updateEntity(
                request,
                entity
        );

        CandidatePreference saved = repository.save(entity);

        completionScoreService.recalculate(profile);

        return mapper.toResponse(saved);

    }

    public void delete(
            UUID userId
    ) {

        profileService.assertProfileOwner(userId);

        Profile profile = profileService.getByUserId(userId);

        CandidatePreference entity = repository
                .findByProfileIdAndDeletedAtIsNull(profile.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate preference not found"
                        ));

        entity.setDeletedAt(LocalDateTime.now());

        repository.save(entity);

        completionScoreService.recalculate(profile);

    }

    private void validateSalary(
            BigDecimal salaryMinimum,
            BigDecimal salaryMaximum
    ) {

        if (salaryMinimum != null
                && salaryMaximum != null
                && salaryMinimum.compareTo(salaryMaximum) > 0) {

            throw new IllegalArgumentException(
                    "Minimum salary cannot be greater than maximum salary."
            );

        }

    }

}
