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

 @Transactional(readOnly = true)
 public Page<ProfileAssetResponse> getAll(
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
 public ProfileAssetResponse getById(
         UUID assetId
 ) {

  ProfileAsset entity = repository
          .findByIdAndDeletedAtIsNull(assetId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Asset not found"
                  ));

  return mapper.toResponse(entity);

 }

 public ProfileAssetResponse upload(
         UUID userId,
         MultipartFile file,
         ProfileAssetKind kind
 ) {

  Profile profile = profileService.getByUserId(userId);

  if (file == null || file.isEmpty()) {
   throw new IllegalArgumentException("File is required.");
  }

  if (file.getSize() > 10 * 1024 * 1024) {
   throw new IllegalArgumentException("Maximum file size is 10MB.");
  }

  if (file.getContentType() == null || file.getContentType().isBlank()) {
   throw new IllegalArgumentException("Content type is required.");
  }

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

  String objectName =
          userId +
                  "/" +
                  kind.name().toLowerCase() +
                  "/" +
                  UUID.randomUUID() +
                  "-" +
                  file.getOriginalFilename();

  storageService.upload(
          file,
          objectName
  );

  ProfileAsset asset = new ProfileAsset();

  asset.setProfile(profile);

  asset.setAssetKind(kind);

  asset.setStorageKey(objectName);

  asset.setOriginalFilename(file.getOriginalFilename());

  asset.setContentType(file.getContentType());

  asset.setSizeBytes(file.getSize());

  asset.setAssetStatus(ProfileAssetStatus.ACTIVE);

  asset.setPublicUrl(
          storageService.getPresignedUrl(objectName)
  );

  ProfileAsset saved = repository.save(asset);

  completionScoreService.recalculate(profile);

  return mapper.toResponse(saved);

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

  return storageService.download(
          entity.getStorageKey()
  );

 }

 @Transactional(readOnly = true)
 public ProfileAssetResponse getAvatar(
         UUID userId
 ) {

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

  storageService.delete(
          entity.getStorageKey()
  );

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