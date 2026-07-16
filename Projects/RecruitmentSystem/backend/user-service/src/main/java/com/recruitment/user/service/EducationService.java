package com.recruitment.user.service;

import com.recruitment.user.dto.request.CreateEducationRequest;
import com.recruitment.user.dto.request.UpdateEducationRequest;
import com.recruitment.user.dto.response.EducationResponse;
import com.recruitment.user.entity.Education;
import com.recruitment.user.entity.Profile;
import com.recruitment.user.exception.ResourceNotFoundException;
import com.recruitment.user.mapper.EducationMapper;
import com.recruitment.user.repository.EducationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EducationService {

 private final EducationRepository repository;
 private final EducationMapper mapper;
 private final ProfileService profileService;
 private final CompletionScoreService completionScoreService;

 @Transactional(readOnly = true)
 public Page<EducationResponse> getAll(
         UUID userId,
         Pageable pageable
 ) {

  Profile profile = profileService.getByUserId(userId);

  return repository.findByProfileIdAndDeletedAtIsNull(
          profile.getId(),
          pageable
  ).map(mapper::toResponse);

 }

 @Transactional(readOnly = true)
 public EducationResponse getById(UUID educationId) {

  Education education = repository
          .findByIdAndDeletedAtIsNull(educationId)
          .orElseThrow(() ->
                  new ResourceNotFoundException("Education not found"));

  return mapper.toResponse(education);

 }

 public EducationResponse create(
         UUID userId,
         CreateEducationRequest request
 ) {

  Profile profile = profileService.getByUserId(userId);

  // Business Validation
  validateDate(request.getStartDate(), request.getEndDate());

  boolean exists = repository
          .existsByProfileIdAndInstitutionNameAndQualificationAndStartDateAndDeletedAtIsNull(
                  profile.getId(),
                  request.getInstitutionName(),
                  request.getQualification(),
                  request.getStartDate()
          );

  if (exists) {
   throw new IllegalArgumentException(
           "Education already exists."
   );
  }

  Education education = mapper.toEntity(request);

  education.setProfile(profile);

  Education saved = repository.save(education);

  completionScoreService.recalculate(profile);

  return mapper.toResponse(saved);

 }

 public EducationResponse update(
         UUID educationId,
         UpdateEducationRequest request
 ) {

  Education education = repository
          .findByIdAndDeletedAtIsNull(educationId)
          .orElseThrow(() ->
                  new ResourceNotFoundException("Education not found"));

  // Business Validation
  validateDate(request.getStartDate(), request.getEndDate());

  mapper.updateEntity(request, education);

  Education saved = repository.save(education);

  completionScoreService.recalculate(saved.getProfile());

  return mapper.toResponse(saved);

 }

 public void delete(UUID educationId) {

  Education education = repository
          .findByIdAndDeletedAtIsNull(educationId)
          .orElseThrow(() ->
                  new ResourceNotFoundException("Education not found"));

  education.setDeletedAt(LocalDateTime.now());

  repository.save(education);

  completionScoreService.recalculate(education.getProfile());

 }

 /**
  * Business validation:
  * End date must not be before start date.
  */
 private void validateDate(
         java.time.LocalDate startDate,
         java.time.LocalDate endDate
 ) {

  if (endDate != null && endDate.isBefore(startDate)) {
   throw new IllegalArgumentException(
           "End date must be after or equal to start date."
   );
  }

 }

}