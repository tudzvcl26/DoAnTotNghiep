package com.recruitment.ai.storage;

import java.io.InputStream;

public interface AiStorageService {

    void initialize();

    boolean bucketExists();

    String bucketName();

    void upload(String objectKey, InputStream inputStream, long size, String contentType);

    byte[] download(String objectKey);

    boolean objectExists(String objectKey);

    void delete(String objectKey);

}
