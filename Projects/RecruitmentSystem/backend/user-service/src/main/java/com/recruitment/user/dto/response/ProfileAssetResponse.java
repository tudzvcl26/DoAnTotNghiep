package com.recruitment.user.dto.response;
import com.recruitment.user.entity.*; import lombok.Builder; import lombok.Getter; import java.util.UUID;
@Getter @Builder public class ProfileAssetResponse { private UUID id; private ProfileAssetKind assetKind; private String originalFilename; private String contentType; private Long sizeBytes; private String publicUrl; private ProfileAssetStatus assetStatus; private Long version; }
