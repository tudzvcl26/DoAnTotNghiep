package com.recruitment.application.service.impl;

import com.recruitment.application.config.ResumeStorageProperties;
import com.recruitment.application.exception.ErrorCode;
import com.recruitment.application.exception.ResourceNotFoundException;
import com.recruitment.application.service.ResumeSnapshotStorage;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioResumeSnapshotStorage implements ResumeSnapshotStorage {
    private final MinioClient resumeMinioClient;
    private final ResumeStorageProperties properties;

    @Override
    public byte[] download(String storageKey) {
        try (InputStream input = resumeMinioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(storageKey)
                .build())) {
            return input.readAllBytes();
        } catch (ErrorResponseException exception) {
            if (exception.response() != null
                    && ("NoSuchKey".equals(exception.response().code())
                    || "NoSuchObject".equals(exception.response().code()))) {
                throw new ResourceNotFoundException(ErrorCode.APPLICATION_RESUME_NOT_FOUND);
            }
            throw new IllegalStateException("Resume snapshot storage is unavailable.", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Resume snapshot download failed.", exception);
        }
    }
}
