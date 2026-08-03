package com.recruitment.ai.storage;

import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioAiStorageService implements AiStorageService {

    private final MinioClient minioClient;
    private final AiStorageProperties properties;

    @Override
    public void initialize() {
        try {
            if (bucketExists()) {
                log.info("AI storage bucket is ready: {}", properties.getBucket());
                return;
            }
            if (!properties.isAutoCreateBucket()) {
                throw new BusinessException(ErrorCode.STORAGE_UNAVAILABLE);
            }
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
            log.info("Created AI storage bucket: {}", properties.getBucket());
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Cannot initialize AI storage bucket {}", properties.getBucket(), exception);
            throw new BusinessException(ErrorCode.STORAGE_UNAVAILABLE);
        }
    }

    @Override
    public boolean bucketExists() {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(properties.getBucket()).build());
        } catch (Exception exception) {
            log.warn("AI storage health check failed for bucket {}: {}",
                    properties.getBucket(), exception.getClass().getSimpleName());
            return false;
        }
    }

    @Override
    public String bucketName() {
        return properties.getBucket();
    }

    @Override
    public void upload(String objectKey, InputStream inputStream, long size, String contentType) {
        try (inputStream) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception exception) {
            log.error("Cannot upload AI storage object {}", objectKey, exception);
            throw new BusinessException(ErrorCode.STORAGE_UNAVAILABLE);
        }
    }

    @Override
    public byte[] download(String objectKey) {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(objectKey)
                .build())) {
            return inputStream.readAllBytes();
        } catch (Exception exception) {
            log.error("Cannot download AI storage object {}", objectKey, exception);
            throw new BusinessException(ErrorCode.STORAGE_UNAVAILABLE);
        }
    }

    @Override
    public boolean objectExists(String objectKey) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception exception) {
            log.error("Cannot delete AI storage object {}", objectKey, exception);
            throw new BusinessException(ErrorCode.STORAGE_UNAVAILABLE);
        }
    }

}
