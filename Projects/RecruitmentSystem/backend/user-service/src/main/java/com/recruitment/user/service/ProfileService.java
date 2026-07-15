package com.recruitment.user.service;

import com.recruitment.user.dto.request.UpdateProfileRequest;
import com.recruitment.user.dto.response.ProfileResponse;
import com.recruitment.user.entity.Profile;
import com.recruitment.user.entity.ProfileStatus;
import com.recruitment.user.entity.ProfileVisibility;
import com.recruitment.user.exception.ResourceNotFoundException;
import com.recruitment.user.mapper.ProfileMapper;
import com.recruitment.user.repository.ProfileRepository;
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
public class ProfileService {

 private final ProfileRepository repository;
 private final ProfileMapper mapper;

 @Transactional(readOnly = true)
 public Profile getByUserId(UUID userId) {

  return repository.findByUserIdAndDeletedAtIsNull(userId)
          .orElseThrow(() ->
                  new ResourceNotFoundException("Profile not found"));

 }

 @Transactional(readOnly = true)
 public ProfileResponse getProfile(UUID userId) {

  return mapper.toResponse(getByUserId(userId));

 }

 @Transactional(readOnly = true)
 public Page<ProfileResponse> search(Pageable pageable) {

  return repository.findAllByDeletedAtIsNull(pageable)
          .map(mapper::toResponse);

 }

 public Profile initialize(
         UUID userId,
         String displayName
 ) {

  return repository.findByUserIdAndDeletedAtIsNull(userId)
          .orElseGet(() -> {

           Profile profile = Profile.builder()
                   .userId(userId)
                   .displayName(displayName)
                   .profileStatus(ProfileStatus.ACTIVE)
                   .profileVisibility(ProfileVisibility.PUBLIC)
                   .completionScore(0)
                   .completionCalculatedAt(LocalDateTime.now())
                   .build();

           return repository.save(profile);

          });

 }

 public ProfileResponse update(
         UUID userId,
         UpdateProfileRequest request
 ) {

  Profile profile = getByUserId(userId);

  mapper.updateEntity(request, profile);

  profile.setCompletionCalculatedAt(LocalDateTime.now());

  Profile saved = repository.save(profile);

  return mapper.toResponse(saved);

 }

 public void updateCompletionScore(
         UUID userId,
         Integer completionScore
 ) {

  Profile profile = getByUserId(userId);

  profile.setCompletionScore(completionScore);
  profile.setCompletionCalculatedAt(LocalDateTime.now());

  repository.save(profile);

 }

 public void activate(UUID userId) {

  Profile profile = getByUserId(userId);

  profile.setProfileStatus(ProfileStatus.ACTIVE);

  repository.save(profile);

 }

 public void hide(UUID userId) {

  Profile profile = getByUserId(userId);

  profile.setProfileVisibility(ProfileVisibility.PRIVATE);

  repository.save(profile);

 }

 public void unhide(UUID userId) {

  Profile profile = getByUserId(userId);

  profile.setProfileVisibility(ProfileVisibility.PUBLIC);

  repository.save(profile);

 }

 public void delete(UUID userId) {

  Profile profile = getByUserId(userId);

  profile.setDeletedAt(LocalDateTime.now());
  profile.setProfileStatus(ProfileStatus.DELETED);

  repository.save(profile);

 }

}