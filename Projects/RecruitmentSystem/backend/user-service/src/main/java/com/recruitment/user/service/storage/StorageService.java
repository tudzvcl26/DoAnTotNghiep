package com.recruitment.user.service.storage;

public interface StorageService {

    String upload(
            byte[] content,
            String objectName,
            String contentType
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
