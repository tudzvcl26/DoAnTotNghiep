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

  return mapper.toResponse(experience);

 }

 public ExperienceResponse create(
         UUID userId,
         CreateExperienceRequest request
 ) {

  Profile profile = profileService.getByUserId(userId);

  boolean exists =
          Boolean.TRUE.equals(request.getCurrent()) &&
                  repository.existsByProfileIdAndEmployerNameAndJobTitleAndCurrentTrueAndDeletedAtIsNull(
                          profile.getId(),
                          request.getEmployerName(),
                          request.getJobTitle()
                  );

  if (exists) {
   throw new IllegalArgumentException(
           "Current experience already exists."
   );
  }

  Experience experience = mapper.toEntity(request);

  experience.setProfile(profile);

  Experience saved = repository.save(experience);

  completionScoreService.recalculate(saved.getProfile());

  return mapper.toResponse(saved);

 }

 public ExperienceResponse update(
         UUID experienceId,
         UpdateExperienceRequest request
 ) {

  Experience experience = repository
          .findByIdAndDeletedAtIsNull(experienceId)
          .orElseThrow(() ->
                  new ResourceNotFoundException("Experience not found"));

  mapper.updateEntity(request, experience);


  Experience saved = repository.save(experience);

  completionScoreService.recalculate(saved.getProfile());

  return mapper.toResponse(saved);

 }

 public void delete(UUID experienceId) {

  Experience experience = repository
          .findByIdAndDeletedAtIsNull(experienceId)
          .orElseThrow(() ->
                  new ResourceNotFoundException("Experience not found"));

  experience.setDeletedAt(LocalDateTime.now());

  repository.save(experience);

  completionScoreService.recalculate(experience.getProfile());

 }

}