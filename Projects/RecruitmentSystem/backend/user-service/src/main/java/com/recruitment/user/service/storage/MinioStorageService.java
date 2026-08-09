package com.recruitment.user.service.storage;

import com.recruitment.user.config.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final StorageProperties storageProperties;

    @PostConstruct
    public void init() {

        if (!storageProperties.isAutoCreateBucket()) {
            return;
        }

        try {

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .build()
            );

            if (!exists) {

                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(storageProperties.getBucket())
                                .build()
                );

            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Cannot initialize MinIO bucket.",
                    e
            );

        }

    }

    @Override
    public String upload(
            byte[] content,
            String objectName,
            String contentType
    ) {

        try (InputStream inputStream = new ByteArrayInputStream(content)) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(objectName)
                            .stream(
                                    inputStream,
                                    content.length,
                                    -1
                            )
                            .contentType(contentType)
                            .build()
            );

            return objectName;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Upload failed.",
                    e
            );

        }

    }
    @Override
    public byte[] download(
            String objectName
    ) {

        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(storageProperties.getBucket())
                        .object(objectName)
                        .build()
        )) {

            return inputStream.readAllBytes();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Download failed.",
                    e
            );

        }

    }

    @Override
    public void delete(
            String objectName
    ) {

        try {

            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(objectName)
                            .build()
            );

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Delete failed.",
                    e
            );

        }

    }

    @Override
    public String getPresignedUrl(
            String objectName
    ) {

        try {

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(storageProperties.getBucket())
                            .object(objectName)
                            .expiry(
                                    7,
                                    TimeUnit.DAYS
                            )
                            .build()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Cannot generate presigned URL.",
                    e
            );

        }

    }

}
