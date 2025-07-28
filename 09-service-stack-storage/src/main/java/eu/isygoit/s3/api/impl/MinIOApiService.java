package eu.isygoit.s3.api.impl;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.exception.MinIoS3BucketException;
import eu.isygoit.s3.api.IMinIOApiService;
import eu.isygoit.s3.config.S3Config;
import eu.isygoit.s3.object.FileStorage;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The type Min io api service.
 */
@Slf4j
public abstract class MinIOApiService implements IMinIOApiService {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int DEFAULT_PRESIGNED_URL_EXPIRY_HOURS = 2;

    private final Map<String, MinioClient> minIoMap;

    /**
     * Instantiates a new Min io api service.
     *
     * @param minIoMap the min io map
     */
    public MinIOApiService(Map<String, MinioClient> minIoMap) {
        this.minIoMap = minIoMap;
    }

    @Override
    public MinioClient getConnection(S3Config config) {
        validateConfig(config);
        return minIoMap.computeIfAbsent(config.getTenant(), k -> {
            try {
                return MinioClient.builder()
                        .endpoint(config.getUrl())
                        .credentials(config.getUserName(), config.getPassword())
                        .build();
            } catch (Exception e) {
                log.error("Failed to create MinIO client for tenant: {}", config.getTenant(), e);
                throw new MinIoS3BucketException("Failed to initialize MinIO client", e);
            }
        });
    }

    @Override
    public void updateConnection(S3Config config) {
        validateConfig(config);
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(config.getUrl())
                    .credentials(config.getUserName(), config.getPassword())
                    .build();
            minIoMap.put(config.getTenant(), client);
            log.info("Updated MinIO connection for tenant: {}", config.getTenant());
        } catch (Exception e) {
            log.error("Failed to update MinIO connection for tenant: {}", config.getTenant(), e);
            throw new MinIoS3BucketException("Failed to update MinIO connection", e);
        }
    }

    @Override
    public boolean bucketExists(S3Config config, String bucketName) {
        validateBucketName(bucketName);
        return executeWithRetry(() -> {
            try {
                MinioClient client = getConnection(config);
                return client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error checking bucket existence", e);
            }
        });
    }

    @Override
    public void setVersioningBucket(S3Config config, String bucketName, boolean status) {
        validateBucketName(bucketName);
        executeWithRetry(() -> {
            try {
                MinioClient client = getConnection(config);
                VersioningConfiguration versionConfig = new VersioningConfiguration(
                        status ? VersioningConfiguration.Status.ENABLED : VersioningConfiguration.Status.SUSPENDED, true);
                client.setBucketVersioning(
                        SetBucketVersioningArgs.builder().bucket(bucketName).config(versionConfig).build());
                log.info("Set versioning {} for bucket: {}", status ? "enabled" : "suspended", bucketName);
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error setting bucket versioning", e);
            }
            return null;
        });
    }

    @Override
    public void makeBucket(S3Config config, String bucketName) {
        validateBucketName(bucketName);
        executeWithRetry(() -> {
            try {
                if (!bucketExists(config, bucketName)) {
                    MinioClient client = getConnection(config);
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    log.info("Created bucket: {}", bucketName);
                }
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error creating bucket: " + bucketName, e);
            }
            return null;
        });
    }

    @Override
    public void uploadFile(S3Config config, String bucketName, String path, String objectName,
                           MultipartFile file, Map<String, String> tags) {
        validateUploadParams(bucketName, path, objectName, file);
        executeWithRetry(() -> {
            try {
                makeBucket(config, bucketName);
                MinioClient client = getConnection(config);
                String fullPath = StringUtils.hasText(path) ? path + "/" + objectName : objectName;
                client.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fullPath)
                        .tags(tags)
                        .contentType(file.getContentType())
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .build());
                log.info("Uploaded file: {} to bucket: {}", fullPath, bucketName);
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error uploading file: " + objectName, e);
            }
            return null;
        });
    }

    @Override
    public byte[] getObject(S3Config config, String bucketName, String objectName, String versionID) {
        validateObjectParams(bucketName, objectName);
        return executeWithRetry(() -> {
            try {
                MinioClient client = getConnection(config);
                GetObjectArgs.Builder args = GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName);
                if (StringUtils.hasText(versionID)) {
                    args.versionId(versionID);
                }
                try (InputStream stream = client.getObject(args.build())) {
                    return IOUtils.toByteArray(stream);
                }
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error retrieving object: " + objectName, e);
            }
        });
    }

    @Override
    public String getPresignedObjectUrl(S3Config config, String bucketName, String objectName) {
        validateObjectParams(bucketName, objectName);
        return executeWithRetry(() -> {
            try {
                MinioClient client = getConnection(config);
                return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(DEFAULT_PRESIGNED_URL_EXPIRY_HOURS, TimeUnit.HOURS)
                        .build());
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error generating presigned URL for: " + objectName, e);
            }
        });
    }

    @Override
    public void deleteObject(S3Config config, String bucketName, String objectName) {
        validateObjectParams(bucketName, objectName);
        executeWithRetry(() -> {
            try {
                MinioClient client = getConnection(config);
                client.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
                log.info("Deleted object: {} from bucket: {}", objectName, bucketName);
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error deleting object: " + objectName, e);
            }
            return null;
        });
    }

    @Override
    public List<FileStorage> getObjectByTags(S3Config config, String bucketName,
                                             Map<String, String> tags, IEnumLogicalOperator.Types condition) {
        validateBucketName(bucketName);
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("Tags cannot be null or empty");
        }
        return executeWithRetry(() -> {
            try {
                List<FileStorage> listFileStorage = new ArrayList<>();
                List<FileStorage> allObjects = getObjects(config, bucketName);
                MinioClient client = getConnection(config);

                for (FileStorage object : allObjects) {
                    Tags tagsList = client.getObjectTags(GetObjectTagsArgs.builder()
                            .bucket(bucketName)
                            .object(object.objectName)
                            .build());

                    boolean accepted = condition == IEnumLogicalOperator.Types.AND
                            ? tagsList.get().entrySet().containsAll(tags.entrySet())
                            : !Collections.disjoint(tagsList.get().values(), tags.values());

                    if (accepted) {
                        object.tags = tagsList.get().values().stream()
                                .filter(tags.values()::contains)
                                .distinct()
                                .collect(Collectors.toList());
                        listFileStorage.add(object);
                    }
                }
                return listFileStorage;
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error retrieving objects by tags", e);
            }
        });
    }

    @Override
    public List<FileStorage> getObjects(S3Config config, String bucketName) {
        validateBucketName(bucketName);
        return executeWithRetry(() -> {
            try {
                MinioClient client = getConnection(config);
                Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .includeVersions(true)
                        .recursive(true)
                        .build());
                List<FileStorage> listFileStorage = new ArrayList<>();
                for (Result<Item> result : results) {
                    Item item = result.get();
                    FileStorage fileObject = new FileStorage();
                    fileObject.objectName = item.objectName();
                    fileObject.size = item.size();
                    fileObject.etag = item.etag();
                    fileObject.lastModified = item.lastModified();
                    fileObject.versionID = item.versionId();
                    fileObject.currentVersion = item.isLatest();
                    listFileStorage.add(fileObject);
                }
                return listFileStorage;
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error listing objects in bucket: " + bucketName, e);
            }
        });
    }

    @Override
    public void updateTags(S3Config config, String bucketName, String objectName, Map<String, String> tags) {
        validateObjectParams(bucketName, objectName);
        if (tags == null) {
            throw new IllegalArgumentException("Tags cannot be null");
        }
        executeWithRetry(() -> {
            try {
                MinioClient client = getConnection(config);
                client.setObjectTags(SetObjectTagsArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .tags(tags)
                        .build());
                log.info("Updated tags for object: {} in bucket: {}", objectName, bucketName);
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error updating tags for object: " + objectName, e);
            }
            return null;
        });
    }

    @Override
    public void deleteObjects(S3Config config, String bucketName, List<DeleteObject> objects) {
        validateBucketName(bucketName);
        if (objects == null || objects.isEmpty()) {
            throw new IllegalArgumentException("Objects list cannot be null or empty");
        }
        executeWithRetry(() -> {
            try {
                MinioClient client = getConnection(config);
                Iterable<Result<DeleteError>> results = client.removeObjects(
                        RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
                List<DeleteError> errors = new ArrayList<>();
                for (Result<DeleteError> result : results) {
                    errors.add(result.get());
                }
                if (!errors.isEmpty()) {
                    log.error("Errors occurred while deleting objects: {}", errors);
                    throw new MinIoS3BucketException("Failed to delete some objects");
                }
                log.info("Deleted {} objects from bucket: {}", objects.size(), bucketName);
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error deleting objects from bucket: " + bucketName, e);
            }
            return null;
        });
    }

    @Override
    public void deleteBucket(S3Config config, String bucketName) {
        validateBucketName(bucketName);
        executeWithRetry(() -> {
            try {
                if (bucketExists(config, bucketName)) {
                    MinioClient client = getConnection(config);
                    client.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
                    log.info("Deleted bucket: {}", bucketName);
                }
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error deleting bucket: " + bucketName, e);
            }
            return null;
        });
    }

    @Override
    public List<Bucket> getBuckets(S3Config config) {
        validateConfig(config);
        return executeWithRetry(() -> {
            try {
                MinioClient client = getConnection(config);
                List<Bucket> buckets = client.listBuckets();
                log.info("Retrieved {} buckets for tenant: {}", buckets.size(), config.getTenant());
                return buckets;
            } catch (Exception e) {
                throw new MinIoS3BucketException("Error listing buckets", e);
            }
        });
    }

    private <T> T executeWithRetry(Supplier<T> operation) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (MinIoS3BucketException e) {
                attempt++;
                if (attempt == MAX_RETRIES) {
                    throw e;
                }
                try {
                    Thread.sleep((long) RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new MinIoS3BucketException("Retry interrupted", ie);
                }
                log.warn("Retrying operation, attempt {}/{}", attempt, MAX_RETRIES);
            }
        }
        throw new MinIoS3BucketException("Operation failed after maximum retries");
    }

    private void validateConfig(S3Config config) {
        if (config == null || !StringUtils.hasText(config.getTenant()) ||
                !StringUtils.hasText(config.getUrl()) ||
                !StringUtils.hasText(config.getUserName()) ||
                !StringUtils.hasText(config.getPassword())) {
            throw new IllegalArgumentException("Invalid storage configuration");
        }
    }

    private void validateBucketName(String bucketName) {
        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalArgumentException("Bucket name cannot be empty");
        }
    }

    private void validateObjectParams(String bucketName, String objectName) {
        validateBucketName(bucketName);
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("Object name cannot be empty");
        }
    }

    private void validateUploadParams(String bucketName, String path, String objectName, MultipartFile file) {
        validateObjectParams(bucketName, objectName);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Multipart file cannot be null or empty");
        }
    }

    @FunctionalInterface
    private interface Supplier<T> {
        /**
         * Get t.
         *
         * @return the t
         * @throws MinIoS3BucketException the min io s 3 bucket exception
         */
        T get() throws MinIoS3BucketException;
    }
}