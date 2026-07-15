package com.recruitment.user.repository;

import com.recruitment.user.entity.ProfileAsset;
import com.recruitment.user.entity.ProfileAssetKind;
import com.recruitment.user.entity.ProfileAssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProfileAssetRepository extends JpaRepository<ProfileAsset, UUID>,
        JpaSpecificationExecutor<ProfileAsset> {

    Page<ProfileAsset> findByProfileIdAndDeletedAtIsNull(
            UUID profileId,
            Pageable pageable
    );

    Optional<ProfileAsset> findByIdAndDeletedAtIsNull(
            UUID id
    );

    Optional<ProfileAsset> findByProfileIdAndAssetKindAndAssetStatusAndDeletedAtIsNull(
            UUID profileId,
            ProfileAssetKind assetKind,
            ProfileAssetStatus assetStatus
    );

}