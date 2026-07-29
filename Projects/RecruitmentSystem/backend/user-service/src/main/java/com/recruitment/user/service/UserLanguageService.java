package com.recruitment.user.service;

import com.recruitment.user.dto.request.CreateLanguageRequest;
import com.recruitment.user.dto.request.UpdateLanguageRequest;
import com.recruitment.user.dto.response.LanguageResponse;
import com.recruitment.user.entity.Language;
import com.recruitment.user.entity.Profile;
import com.recruitment.user.entity.UserLanguage;
import com.recruitment.user.exception.ResourceNotFoundException;
import com.recruitment.user.mapper.UserLanguageMapper;
import com.recruitment.user.repository.LanguageRepository;
import com.recruitment.user.repository.UserLanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserLanguageService {

 private final UserLanguageRepository userLanguageRepository;
 private final LanguageRepository languageRepository;
 private final UserLanguageMapper mapper;
 private final ProfileService profileService;
 private final CompletionScoreService completionScoreService;

 private String normalizeCode(String value) {

  if (value == null) {
   return "";
  }

  return value
          .trim()
          .toUpperCase(Locale.ROOT)
          .replace(' ', '_');

 }

 @Transactional(readOnly = true)
 public Page<LanguageResponse> getAll(
         UUID userId,
         Pageable pageable
 ) {

  Profile profile = profileService.getByUserId(userId);

  return userLanguageRepository
          .findByProfileIdAndDeletedAtIsNull(
                  profile.getId(),
                  pageable
          )
          .map(mapper::toResponse);

 }

 @Transactional(readOnly = true)
 public LanguageResponse getById(
         UUID userLanguageId
 ) {

  UserLanguage entity = userLanguageRepository
          .findByIdAndDeletedAtIsNull(userLanguageId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Language not found"
                  ));

  return mapper.toResponse(entity);

 }

 public LanguageResponse create(
         UUID userId,
         CreateLanguageRequest request
 ) {

  profileService.assertProfileOwner(userId);

  Profile profile = profileService.getByUserId(userId);

  String languageCode = normalizeCode(
          request.getLanguageCode()
  );

  Language language = languageRepository
          .findByLanguageCodeAndDeletedAtIsNull(languageCode)
          .orElseGet(() ->
                  languageRepository.save(
                          Language.builder()
                                  .languageCode(languageCode)
                                  .displayName(languageCode)
                                  .build()
                  ));

  if (userLanguageRepository.existsByProfileIdAndLanguageIdAndDeletedAtIsNull(
          profile.getId(),
          language.getId()
  )) {

   throw new IllegalArgumentException(
           "Language already exists."
   );

  }

  UserLanguage entity = mapper.toEntity(request);

  entity.setProfile(profile);
  entity.setLanguage(language);

  UserLanguage saved = userLanguageRepository.save(entity);

  completionScoreService.recalculate(profile);

  return mapper.toResponse(saved);

 }

 public LanguageResponse update(
         UUID userLanguageId,
         UpdateLanguageRequest request
 ) {

  UserLanguage entity = userLanguageRepository
          .findByIdAndDeletedAtIsNull(userLanguageId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Language not found"
                  ));

  profileService.assertProfileOwner(entity.getProfile().getUserId());

  if (request.getLanguageCode() != null &&
          !request.getLanguageCode().isBlank()) {

   String languageCode = normalizeCode(
           request.getLanguageCode()
   );

   Language language = languageRepository
           .findByLanguageCodeAndDeletedAtIsNull(languageCode)
           .orElseGet(() ->
                   languageRepository.save(
                           Language.builder()
                                   .languageCode(languageCode)
                                   .displayName(languageCode)
                                   .build()
                   ));

   entity.setLanguage(language);

  }

  mapper.updateEntity(request, entity);

  UserLanguage saved = userLanguageRepository.save(entity);

  completionScoreService.recalculate(saved.getProfile());

  return mapper.toResponse(saved);

 }

 public void delete(
         UUID userLanguageId
 ) {

  UserLanguage entity = userLanguageRepository
          .findByIdAndDeletedAtIsNull(userLanguageId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Language not found"
                  ));

  profileService.assertProfileOwner(entity.getProfile().getUserId());

  entity.setDeletedAt(LocalDateTime.now());

  userLanguageRepository.save(entity);

  completionScoreService.recalculate(entity.getProfile());

 }

}