package com.recruitment.user.service;

import com.recruitment.user.dto.response.ProfileAssetResponse;
import com.recruitment.user.entity.Profile;
import com.recruitment.user.entity.ProfileAsset;
import com.recruitment.user.entity.ProfileAssetKind;
import com.recruitment.user.entity.ProfileAssetStatus;
import com.recruitment.user.exception.ResourceNotFoundException;
import com.recruitment.user.mapper.ProfileAssetMapper;
import com.recruitment.user.repository.ProfileAssetRepository;
import com.recruitment.user.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileAssetService {

 private final ProfileAssetRepository repository;
 private final ProfileAssetMapper mapper;
 private final ProfileService profileService;
 private final StorageService storageService;
 private final CompletionScoreService completionScoreService;
 private final ProfileAssetFileValidator fileValidator;

 @Transactional(readOnly = true)
 public Page<ProfileAssetResponse> getAll(
         UUID userId,
         Pageable pageable
 ) {

  profileService.assertProfileOwner(userId);

  Profile profile = profileService.getByUserId(userId);

  return repository
          .findByProfileIdAndDeletedAtIsNull(
                  profile.getId(),
                  pageable
          )
          .map(mapper::toResponse);

 }

 @Transactional(readOnly = true)
 public ProfileAssetResponse getById(
         UUID assetId
 ) {

  ProfileAsset entity = repository
          .findByIdAndDeletedAtIsNull(assetId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Asset not found"
                  ));

  profileService.assertProfileOwner(entity.getProfile().getUserId());

  return mapper.toResponse(entity);

 }

 public ProfileAssetResponse upload(
         UUID userId,
         MultipartFile file,
         ProfileAssetKind kind
 ) {

  profileService.assertProfileOwner(userId);

  Profile profile = profileService.getByUserId(userId);

  ValidatedProfileAssetFile validatedFile = fileValidator.validate(file, kind);

  /*
   * Mỗi profile chỉ có 1 avatar ACTIVE
   */
  if (kind == ProfileAssetKind.AVATAR) {

   repository.findByProfileIdAndAssetKindAndAssetStatusAndDeletedAtIsNull(
           profile.getId(),
           ProfileAssetKind.AVATAR,
           ProfileAssetStatus.ACTIVE
   ).ifPresent(oldAvatar -> {

    oldAvatar.setAssetStatus(ProfileAssetStatus.DELETED);
    oldAvatar.setDeletedAt(LocalDateTime.now());

    repository.save(oldAvatar);

   });

  }

  Long assetVersion = null;
  if (kind == ProfileAssetKind.RESUME) {
   repository.findByProfileIdAndAssetKindAndCurrentTrueAndAssetStatusAndDeletedAtIsNull(
           profile.getId(), ProfileAssetKind.RESUME, ProfileAssetStatus.ACTIVE
   ).ifPresent(previous -> {
    previous.setCurrent(false);
    repository.save(previous);
   });
   assetVersion = repository.findFirstByProfileIdAndAssetKindOrderByAssetVersionDesc(
                   profile.getId(), ProfileAssetKind.RESUME
           ).map(previous -> previous.getAssetVersion() + 1L)
           .orElse(1L);
  }

  String objectName =
          userId +
                  "/" +
                  kind.name().toLowerCase() +
                  "/" +
                  UUID.randomUUID() +
                  "." +
                  validatedFile.extension();

  storageService.upload(
          validatedFile.content(),
          objectName,
          validatedFile.contentType()
  );

  ProfileAsset asset = new ProfileAsset();

  asset.setProfile(profile);

  asset.setAssetKind(kind);

  asset.setStorageKey(objectName);

  asset.setOriginalFilename(validatedFile.originalFilename());

  asset.setContentType(validatedFile.contentType());

  asset.setSizeBytes((long) validatedFile.content().length);

  asset.setAssetStatus(ProfileAssetStatus.ACTIVE);

  asset.setAssetVersion(assetVersion);

  asset.setCurrent(kind == ProfileAssetKind.RESUME);

  asset.setChecksum(DigestUtils.sha256Hex(validatedFile.content()));

  asset.setPublicUrl(
          storageService.getPresignedUrl(objectName)
  );

  ProfileAsset saved = repository.save(asset);

  completionScoreService.recalculate(profile);

  return mapper.toResponse(saved);

 }

 @Transactional(readOnly = true)
 public Page<ProfileAssetResponse> getResumes(UUID userId, Pageable pageable) {
  profileService.assertProfileOwner(userId);
  Profile profile = profileService.getByUserId(userId);
  return repository.findByProfileIdAndAssetKindAndDeletedAtIsNull(
          profile.getId(), ProfileAssetKind.RESUME, pageable
  ).map(mapper::toResponse);
 }

 @Transactional(readOnly = true)
 public ProfileAssetResponse getCurrentResume(UUID userId) {
  profileService.assertProfileOwner(userId);
  Profile profile = profileService.getByUserId(userId);
  return mapper.toResponse(repository
          .findByProfileIdAndAssetKindAndCurrentTrueAndAssetStatusAndDeletedAtIsNull(
                  profile.getId(), ProfileAssetKind.RESUME, ProfileAssetStatus.ACTIVE
          ).orElseThrow(() -> new ResourceNotFoundException("Current resume not found")));
 }

 @Transactional(readOnly = true)
 public ProfileAssetResponse getResumeById(UUID userId, UUID assetId) {
  profileService.assertProfileOwner(userId);
  ProfileAsset entity = requireResume(assetId);
  if (!entity.getProfile().getUserId().equals(userId)) {
   throw new ResourceNotFoundException("Resume not found");
  }
  return mapper.toResponse(entity);
 }

 @Transactional(readOnly = true)
 public byte[] downloadResume(UUID userId, UUID assetId) {
  profileService.assertProfileOwner(userId);
  ProfileAsset entity = requireResume(assetId);
  if (!entity.getProfile().getUserId().equals(userId)) {
   throw new ResourceNotFoundException("Resume not found");
  }
  return storageService.download(entity.getStorageKey());
 }

 public void deleteResume(UUID userId, UUID assetId) {
  profileService.assertProfileOwner(userId);
  ProfileAsset entity = requireResume(assetId);
  if (!entity.getProfile().getUserId().equals(userId)) {
   throw new ResourceNotFoundException("Resume not found");
  }
  entity.setCurrent(false);
  entity.setDeletedAt(LocalDateTime.now());
  entity.setAssetStatus(ProfileAssetStatus.DELETED);
  repository.save(entity);
  completionScoreService.recalculate(entity.getProfile());
 }

 private ProfileAsset requireResume(UUID assetId) {
  ProfileAsset entity = repository.findByIdAndDeletedAtIsNull(assetId)
          .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
  if (entity.getAssetKind() != ProfileAssetKind.RESUME) {
   throw new ResourceNotFoundException("Resume not found");
  }
  return entity;
 }

 @Transactional(readOnly = true)
 public byte[] download(
         UUID assetId
 ) {

  ProfileAsset entity = repository
          .findByIdAndDeletedAtIsNull(assetId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Asset not found"
                  ));

  profileService.assertProfileOwner(entity.getProfile().getUserId());

  return storageService.download(
          entity.getStorageKey()
  );

 }

 @Transactional(readOnly = true)
 public ProfileAssetResponse getAvatar(
         UUID userId
 ) {

  profileService.assertProfileOwner(userId);

  Profile profile = profileService.getByUserId(userId);

  ProfileAsset entity = repository
          .findByProfileIdAndAssetKindAndAssetStatusAndDeletedAtIsNull(
                  profile.getId(),
                  ProfileAssetKind.AVATAR,
                  ProfileAssetStatus.ACTIVE
          )
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Avatar not found"
                  ));

  return mapper.toResponse(entity);

 }

 public void delete(
         UUID assetId
 ) {

  ProfileAsset entity = repository
          .findByIdAndDeletedAtIsNull(assetId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Asset not found"
                  ));

  profileService.assertProfileOwner(entity.getProfile().getUserId());

  if (entity.getAssetKind() != ProfileAssetKind.RESUME) {
   storageService.delete(entity.getStorageKey());
  }

  entity.setCurrent(false);

  entity.setDeletedAt(LocalDateTime.now());

  entity.setAssetStatus(
          ProfileAssetStatus.DELETED
  );

  repository.save(entity);

  completionScoreService.recalculate(
          entity.getProfile()
  );

 }

}
