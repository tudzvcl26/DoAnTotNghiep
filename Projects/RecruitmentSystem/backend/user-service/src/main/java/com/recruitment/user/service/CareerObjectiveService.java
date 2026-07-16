package com.recruitment.user.service;

import com.recruitment.user.dto.request.UpdateCareerObjectiveRequest;
import com.recruitment.user.dto.response.CareerObjectiveResponse;
import com.recruitment.user.entity.CareerObjective;
import com.recruitment.user.entity.Profile;
import com.recruitment.user.exception.ResourceNotFoundException;
import com.recruitment.user.mapper.CareerObjectiveMapper;
import com.recruitment.user.repository.CareerObjectiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CareerObjectiveService {

 private final CareerObjectiveRepository repository;
 private final ProfileService profileService;
 private final CareerObjectiveMapper mapper;
 private final CompletionScoreService completionScoreService;

 public CareerObjectiveResponse upsert(
         UUID userId,
         UpdateCareerObjectiveRequest request
 ) {

  Profile profile = profileService.getByUserId(userId);

  CareerObjective entity = repository
          .findByProfileIdAndDeletedAtIsNull(profile.getId())
          .orElseGet(() -> {
           CareerObjective objective = new CareerObjective();
           objective.setProfile(profile);
           return objective;
          });

  mapper.updateEntity(request, entity);

  CareerObjective saved = repository.save(entity);

  completionScoreService.recalculate(profile);

  return mapper.toResponse(saved);

 }

 @Transactional(readOnly = true)
 public CareerObjectiveResponse get(
         UUID userId
 ) {

  Profile profile = profileService.getByUserId(userId);

  CareerObjective entity = repository
          .findByProfileIdAndDeletedAtIsNull(profile.getId())
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Career objective not found"
                  ));

  return mapper.toResponse(entity);

 }

 public void delete(
         UUID userId
 ) {

  Profile profile = profileService.getByUserId(userId);

  CareerObjective entity = repository
          .findByProfileIdAndDeletedAtIsNull(profile.getId())
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Career objective not found"
                  ));

  entity.setDeletedAt(LocalDateTime.now());

  repository.save(entity);

  completionScoreService.recalculate(profile);

 }

}