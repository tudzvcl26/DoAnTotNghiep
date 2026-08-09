package com.recruitment.user.dto.response;
import com.recruitment.user.entity.*; import lombok.Builder; import lombok.Getter; import java.time.LocalDateTime; import java.util.UUID;
@Getter @Builder public class ProfileAssetResponse { private UUID id; private ProfileAssetKind assetKind; private String storageKey; private String originalFilename; private String contentType; private Long sizeBytes; private String checksum; private String publicUrl; private ProfileAssetStatus assetStatus; private Long assetVersion; private boolean current; private LocalDateTime createdAt; private Long version; }
