package com.recruitment.user.service;

record ValidatedProfileAssetFile(
        byte[] content,
        String originalFilename,
        String contentType,
        String extension
) {
}
