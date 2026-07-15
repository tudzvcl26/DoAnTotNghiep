package com.recruitment.user.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String upload(
            MultipartFile file,
            String objectName
    );

    byte[] download(
            String objectName
    );

    void delete(
            String objectName
    );

    String getPresignedUrl(
            String objectName
    );

}