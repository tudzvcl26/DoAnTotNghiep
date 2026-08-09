package com.recruitment.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profile_assets", indexes = {
        @Index(name = "idx_profile_assets_profile_kind_status", columnList = "profile_id,asset_kind,asset_status"),
        @Index(name = "idx_profile_assets_certificate_id", columnList = "certificate_id")
})
public class ProfileAsset extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id")
    private Certificate certificate;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_kind", nullable = false, length = 30)
    private ProfileAssetKind assetKind;

    @NotBlank
    @Size(max = 1024)
    @Column(name = "storage_key", nullable = false, length = 1024)
    private String storageKey;

    @NotBlank
    @Size(max = 255)
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @NotBlank
    @Size(max = 100)
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Positive
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Size(max = 128)
    @Column(length = 128)
    private String checksum;

    @Size(max = 2048)
    @Column(name = "public_url", length = 2048)
    private String publicUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_status", nullable = false, length = 30)
    private ProfileAssetStatus assetStatus;

    @Column(name = "asset_version")
    private Long assetVersion;

    @Column(name = "is_current", nullable = false)
    private boolean current;

}
