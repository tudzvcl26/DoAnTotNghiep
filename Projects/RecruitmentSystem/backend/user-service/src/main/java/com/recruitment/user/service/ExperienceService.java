package com.recruitment.user.service;

import com.recruitment.user.dto.request.CreateExperienceRequest;
import com.recruitment.user.dto.request.UpdateExperienceRequest;
import com.recruitment.user.dto.response.ExperienceResponse;
import com.recruitment.user.entity.Experience;
import com.recruitment.user.entity.Profile;
import com.recruitment.user.exception.ResourceNotFoundException;
import com.recruitment.user.mapper.ExperienceMapper;
import com.recruitment.user.repository.ExperienceRepository;
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
public class ExperienceService {

 private final ExperienceRepository repository;
 private final ExperienceMapper mapper;
 private final ProfileService profileService;
 private final CompletionScoreService completionScoreService;

 @Transactional(readOnly = true)
 public Page<ExperienceResponse> getAll(
         UUID userId,
         Pageable pageable
 ) {

  profileService.assertProfileOwner(userId);

  Profile profile = profileService.getByUserId(userId);

  return repository.findByProfileIdAndDeletedAtIsNull(
          profile.getId(),
          pageable
  ).map(mapper::toResponse);

 }

 @Transactional(readOnly = true)
 public ExperienceResponse getById(UUID experienceId) {

  Experience experience = repository
          .findByIdAndDeletedAtIsNull(experienceId)
          .orElseThrow(() ->
                  new ResourceNotFoundException("Experience not found"));

  profileService.assertProfileOwner(experience.getProfile().getUserId());

  return mapper.toResponse(experience);

 }

 public ExperienceResponse create(
         UUID userId,
         CreateExperienceRequest request
 ) {

  profileService.assertProfileOwner(userId);

  Profile profile = profileService.getByUserId(userId);

  validate(request.getStartDate(),
          request.getEndDate(),
          request.getCurrent());

  boolean exists =
          Boolean.TRUE.equals(request.getCurrent())
                  && repository.existsByProfileIdAndEmployerNameAndJobTitleAndCurrentTrueAndDeletedAtIsNull(
                  profile.getId(),
                  request.getEmployerName(),
                  request.getJobTitle()
          );

  if (exists) {
   throw new IllegalArgumentException(
           "Current experience already exists."
   );
  }

  Experience entity = mapper.toEntity(request);

  entity.setProfile(profile);

  Experience saved = repository.save(entity);

  completionScoreService.recalculate(saved.getProfile());

  return mapper.toResponse(saved);

 }

 public ExperienceResponse update(
         UUID experienceId,
         UpdateExperienceRequest request
 ) {

  Experience entity = repository
          .findByIdAndDeletedAtIsNull(experienceId)
          .orElseThrow(() ->
                  new ResourceNotFoundException("Experience not found"));

  profileService.assertProfileOwner(entity.getProfile().getUserId());

  validate(request.getStartDate(),
          request.getEndDate(),
          request.getCurrent());

  mapper.updateEntity(request, entity);

  Experience saved = repository.save(entity);

  completionScoreService.recalculate(saved.getProfile());

  return mapper.toResponse(saved);

 }

 public void delete(UUID experienceId) {

  Experience entity = repository
          .findByIdAndDeletedAtIsNull(experienceId)
          .orElseThrow(() ->
                  new ResourceNotFoundException("Experience not found"));

  profileService.assertProfileOwner(entity.getProfile().getUserId());

  entity.setDeletedAt(LocalDateTime.now());

  repository.save(entity);

  completionScoreService.recalculate(entity.getProfile());

 }

 private void validate(
         LocalDate startDate,
         LocalDate endDate,
         Boolean current
 ) {

  if (endDate != null && endDate.isBefore(startDate)) {
   throw new IllegalArgumentException(
           "End date must be after or equal to start date."
   );
  }

  if (Boolean.TRUE.equals(current) && endDate != null) {
   throw new IllegalArgumentException(
           "Current job must not have an end date."
   );
  }

 }

}
