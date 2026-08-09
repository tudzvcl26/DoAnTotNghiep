package com.recruitment.application.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeClientDto {
    private UUID id;
    private String storageKey;
    private String originalFilename;
    private String contentType;
    private Long sizeBytes;
    private String checksum;
    private Long assetVersion;
    private String rawJsonData;
}
