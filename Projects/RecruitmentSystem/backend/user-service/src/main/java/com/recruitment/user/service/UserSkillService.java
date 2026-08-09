package com.recruitment.user.service;

import com.recruitment.user.dto.request.CreateSkillRequest;
import com.recruitment.user.dto.request.UpdateSkillRequest;
import com.recruitment.user.dto.response.SkillResponse;
import com.recruitment.user.entity.Profile;
import com.recruitment.user.entity.Skill;
import com.recruitment.user.entity.UserSkill;
import com.recruitment.user.exception.ResourceNotFoundException;
import com.recruitment.user.mapper.UserSkillMapper;
import com.recruitment.user.repository.SkillRepository;
import com.recruitment.user.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSkillService {

 private final UserSkillRepository userSkillRepository;
 private final SkillRepository skillRepository;
 private final UserSkillMapper mapper;
 private final ProfileService profileService;
 private final CompletionScoreService completionScoreService;

 private String normalize(String value) {

  if (value == null) {
   return "";
  }

  String normalized = Normalizer.normalize(
          value,
          Normalizer.Form.NFD
  );

  normalized = normalized.replaceAll("\\p{M}", "");

  return normalized
          .trim()
          .toLowerCase(Locale.ROOT)
          .replaceAll("\\s+", "_");
 }

 @Transactional(readOnly = true)
 public Page<SkillResponse> getAll(
         UUID userId,
         Pageable pageable
 ) {

  profileService.assertProfileOwner(userId);

  Profile profile = profileService.getByUserId(userId);

  return userSkillRepository
          .findByProfileIdAndDeletedAtIsNull(
                  profile.getId(),
                  pageable
          )
          .map(mapper::toResponse);

 }

 @Transactional(readOnly = true)
 public SkillResponse getById(
         UUID userSkillId
 ) {

  UserSkill entity = userSkillRepository
          .findByIdAndDeletedAtIsNull(userSkillId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Skill not found"
                  ));

  profileService.assertProfileOwner(entity.getProfile().getUserId());

  return mapper.toResponse(entity);

 }

 public SkillResponse create(
         UUID userId,
         CreateSkillRequest request
 ) {

  profileService.assertProfileOwner(userId);

  Profile profile = profileService.getByUserId(userId);

  String normalizedKey = normalize(
          request.getSkillName()
  );

  Skill skill = skillRepository
          .findByNormalizedSkillKeyAndDeletedAtIsNull(
                  normalizedKey
          )
          .orElseGet(() ->
                  skillRepository.save(
                          Skill.builder()
                                  .normalizedSkillKey(normalizedKey)
                                  .displayName(request.getSkillName())
                                  .build()
                  ));

  if (userSkillRepository.existsByProfileIdAndSkillIdAndDeletedAtIsNull(
          profile.getId(),
          skill.getId()
  )) {

   throw new IllegalArgumentException(
           "Skill already exists."
   );

  }

  UserSkill entity = mapper.toEntity(request);

  entity.setProfile(profile);

  entity.setSkill(skill);
  UserSkill saved = userSkillRepository.save(entity);

  completionScoreService.recalculate(profile);

  return mapper.toResponse(saved);

 }

 public SkillResponse update(
         UUID userSkillId,
         UpdateSkillRequest request
 ) {

  UserSkill entity = userSkillRepository
          .findByIdAndDeletedAtIsNull(userSkillId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Skill not found"
                  ));

  profileService.assertProfileOwner(entity.getProfile().getUserId());

  if (request.getSkillName() != null &&
          !request.getSkillName().isBlank()) {

   String normalizedKey = normalize(
           request.getSkillName()
   );

   Skill skill = skillRepository
           .findByNormalizedSkillKeyAndDeletedAtIsNull(
                   normalizedKey
           )
           .orElseGet(() ->
                   skillRepository.save(
                           Skill.builder()
                                   .normalizedSkillKey(normalizedKey)
                                   .displayName(request.getSkillName())
                                   .build()
                   ));

   entity.setSkill(skill);

  }

  mapper.updateEntity(
          request,
          entity
  );

  UserSkill saved = userSkillRepository.save(entity);

  completionScoreService.recalculate(saved.getProfile());

  return mapper.toResponse(saved);

 }

 public void delete(
         UUID userSkillId
 ) {

  UserSkill entity = userSkillRepository
          .findByIdAndDeletedAtIsNull(userSkillId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Skill not found"
                  ));

  profileService.assertProfileOwner(entity.getProfile().getUserId());

  entity.setDeletedAt(LocalDateTime.now());

  userSkillRepository.save(entity);

  completionScoreService.recalculate(entity.getProfile());

 }

}
