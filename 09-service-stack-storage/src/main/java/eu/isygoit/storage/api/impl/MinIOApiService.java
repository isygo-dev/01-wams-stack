package eu.isygoit.storage.api.impl;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.api.IMinIOApiService;
import eu.isygoit.storage.exception.MinIoObjectException;
import eu.isygoit.storage.object.FileStorage;
import eu.isygoit.storage.object.StorageConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service implementation for MinIO object storage operations with enhanced error handling,
 * connection pooling, and retry logic.
 */
@Slf4j
@Service
@Transactional
public class MinIOApiService implements IMinIOApiService {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int DEFAULT_PRESIGNED_URL_EXPIRY_HOURS = 2;

    @Autowired
    private Map<String, MinioClient> minIoMap = new ConcurrentHashMap<>();

    /**
     * Retrieves or creates a MinIO client connection for the specified tenant.
     *
     * @param config Storage configuration containing tenant, credentials, and endpoint
     * @return MinioClient instance
     * @throws IllegalArgumentException if config is invalid
     */
    @Override
    public MinioClient getConnection(StorageConfig config) {
        validateConfig(config);
        return minIoMap.computeIfAbsent(config.getTenant(), k -> {
            try {
                return MinioClient.builder()
                        .endpoint(config.getUrl())
                        .credentials(config.getUserName(), config.getPassword())
                        .build();
            } catch (Exception e) {
                log.error("Failed to create MinIO client for tenant: {}", config.getTenant(), e);
                throw new MinIoObjectException("Failed to initialize MinIO client", e);
            }
        });
    }

    /**
     * Updates the MinIO client connection for a tenant.
     *
     * @param config Storage configuration
     * @throws IllegalArgumentException if config is invalid
     */
    @Override
    public void updateConnection(StorageConfig config) {
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
            throw new MinIoObjectException("Failed to update MinIO connection", e);
        }
    }

    /**
     * Checks if a bucket exists with retry logic.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @return true if bucket exists
     * @throws MinIoObjectException on failure
     */
    @Override
    public boolean bucketExists(StorageConfig config, String bucketName) {
        validateBucketName(bucketName);
        return executeWithRetry(() -> {
            try {
                MinioClient client = getConnection(config);
                return client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            } catch (Exception e) {
                throw new MinIoObjectException("Error checking bucket existence", e);
            }
        });
    }

    /**
     * Enables or suspends bucket versioning.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param status     true to enable, false to suspend
     * @throws MinIoObjectException on failure
     */
    @Override
    public void setVersioningBucket(StorageConfig config, String bucketName, boolean status) {
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
                throw new MinIoObjectException("Error setting bucket versioning", e);
            }
            return null;
        });
    }

    /**
     * Creates a bucket if it doesn't exist.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @throws MinIoObjectException on failure
     */
    @Override
    public void makeBucket(StorageConfig config, String bucketName) {
        validateBucketName(bucketName);
        executeWithRetry(() -> {
            try {
                if (!bucketExists(config, bucketName)) {
                    MinioClient client = getConnection(config);
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    log.info("Created bucket: {}", bucketName);
                }
            } catch (Exception e) {
                throw new MinIoObjectException("Error creating bucket: " + bucketName, e);
            }
            return null;
        });
    }

    /**
     * Uploads a file to MinIO with tags.
     *
     * @param config        Storage configuration
     * @param bucketName    Name of the bucket
     * @param path          Object path
     * @param objectName    Object name
     * @param multipartFile File to upload
     * @param tags          Metadata tags
     * @throws MinIoObjectException on failure
     */
    @Override
    public void uploadFile(StorageConfig config, String bucketName, String path, String objectName,
                           MultipartFile multipartFile, Map<String, String> tags) {
        validateUploadParams(bucketName, path, objectName, multipartFile);
        executeWithRetry(() -> {
            try {
                makeBucket(config, bucketName);
                MinioClient client = getConnection(config);
                String fullPath = StringUtils.hasText(path) ? path + "/" + objectName : objectName;
                client.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fullPath)
                        .tags(tags)
                        .contentType(multipartFile.getContentType())
                        .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                        .build());
                log.info("Uploaded file: {} to bucket: {}", fullPath, bucketName);
            } catch (Exception e) {
                throw new MinIoObjectException("Error uploading file: " + objectName, e);
            }
            return null;
        });
    }

    /**
     * Retrieves an object from MinIO.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @param versionID  Version ID (optional)
     * @return Object content as byte array
     * @throws MinIoObjectException on failure
     */
    @Override
    public byte[] getObject(StorageConfig config, String bucketName, String objectName, String versionID) {
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
                throw new MinIoObjectException("Error retrieving object: " + objectName, e);
            }
        });
    }

    /**
     * Generates a presigned URL for an object.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @return Presigned URL
     * @throws MinIoObjectException on failure
     */
    @Override
    public String getPresignedObjectUrl(StorageConfig config, String bucketName, String objectName) {
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
                throw new MinIoObjectException("Error generating presigned URL for: " + objectName, e);
            }
        });
    }

    /**
     * Deletes an object from MinIO.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @throws MinIoObjectException on failure
     */
    @Override
    public void deleteObject(StorageConfig config, String bucketName, String objectName) {
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
                throw new MinIoObjectException("Error deleting object: " + objectName, e);
            }
            return null;
        });
    }

    /**
     * Retrieves objects by tags with AND/OR condition.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param tags       Tags to filter
     * @param condition  Logical operator (AND/OR)
     * @return List of matching FileStorage objects
     * @throws MinIoObjectException on failure
     */
    @Override
    public List<FileStorage> getObjectByTags(StorageConfig config, String bucketName,
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
                throw new MinIoObjectException("Error retrieving objects by tags", e);
            }
        });
    }

    /**
     * Lists all objects in a bucket.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @return List of FileStorage objects
     * @throws MinIoObjectException on failure
     */
    @Override
    public List<FileStorage> getObjects(StorageConfig config, String bucketName) {
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
                throw new MinIoObjectException("Error listing objects in bucket: " + bucketName, e);
            }
        });
    }

    /**
     * Updates tags for an object.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @param tags       New tags
     * @throws MinIoObjectException on failure
     */
    @Override
    public void updateTags(StorageConfig config, String bucketName, String objectName, Map<String, String> tags) {
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
                throw new MinIoObjectException("Error updating tags for object: " + objectName, e);
            }
            return null;
        });
    }

    /**
     * Deletes multiple objects from a bucket.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objects    List of objects to delete
     * @throws MinIoObjectException on failure
     */
    @Override
    public void deleteObjects(StorageConfig config, String bucketName, List<DeleteObject> objects) {
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
                    throw new MinIoObjectException("Failed to delete some objects");
                }
                log.info("Deleted {} objects from bucket: {}", objects.size(), bucketName);
            } catch (Exception e) {
                throw new MinIoObjectException("Error deleting objects from bucket: " + bucketName, e);
            }
            return null;
        });
    }

    /**
     * Deletes a bucket if it exists.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @throws MinIoObjectException on failure
     */
    @Override
    public void deleteBucket(StorageConfig config, String bucketName) {
        validateBucketName(bucketName);
        executeWithRetry(() -> {
            try {
                if (bucketExists(config, bucketName)) {
                    MinioClient client = getConnection(config);
                    client.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
                    log.info("Deleted bucket: {}", bucketName);
                }
            } catch (Exception e) {
                throw new MinIoObjectException("Error deleting bucket: " + bucketName, e);
            }
            return null;
        });
    }

    /**
     * Lists all buckets for the given configuration.
     *
     * @param config Storage configuration
     * @return List of buckets
     * @throws MinIoObjectException on failure
     */
    @Override
    public List<Bucket> getBuckets(StorageConfig config) {
        validateConfig(config);
        return executeWithRetry(() -> {
            try {
                MinioClient client = getConnection(config);
                List<Bucket> buckets = client.listBuckets();
                log.info("Retrieved {} buckets for tenant: {}", buckets.size(), config.getTenant());
                return buckets;
            } catch (Exception e) {
                throw new MinIoObjectException("Error listing buckets", e);
            }
        });
    }

    /**
     * Executes an operation with retry logic.
     *
     * @param operation The operation to execute
     * @param <T>       Return type
     * @return Operation result
     * @throws MinIoObjectException on failure after retries
     */
    private <T> T executeWithRetry(Supplier<T> operation) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (MinIoObjectException e) {
                attempt++;
                if (attempt == MAX_RETRIES) {
                    throw e;
                }
                try {
                    Thread.sleep((long) RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new MinIoObjectException("Retry interrupted", ie);
                }
                log.warn("Retrying operation, attempt {}/{}", attempt, MAX_RETRIES);
            }
        }
        throw new MinIoObjectException("Operation failed after maximum retries");
    }

    /**
     * Validates storage configuration.
     *
     * @param config Storage configuration
     * @throws IllegalArgumentException if invalid
     */
    private void validateConfig(StorageConfig config) {
        if (config == null || !StringUtils.hasText(config.getTenant()) ||
                !StringUtils.hasText(config.getUrl()) ||
                !StringUtils.hasText(config.getUserName()) ||
                !StringUtils.hasText(config.getPassword())) {
            throw new IllegalArgumentException("Invalid storage configuration");
        }
    }

    /**
     * Validates bucket name.
     *
     * @param bucketName Name of the bucket
     * @throws IllegalArgumentException if invalid
     */
    private void validateBucketName(String bucketName) {
        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalArgumentException("Bucket name cannot be empty");
        }
    }

    /**
     * Validates object parameters.
     *
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @throws IllegalArgumentException if invalid
     */
    private void validateObjectParams(String bucketName, String objectName) {
        validateBucketName(bucketName);
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("Object name cannot be empty");
        }
    }

    /**
     * Validates upload parameters.
     *
     * @param bucketName    Name of the bucket
     * @param path          Object path
     * @param objectName    Object name
     * @param multipartFile File to upload
     * @throws IllegalArgumentException if invalid
     */
    private void validateUploadParams(String bucketName, String path, String objectName, MultipartFile multipartFile) {
        validateObjectParams(bucketName, objectName);
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("Multipart file cannot be null or empty");
        }
    }

    @FunctionalInterface
    private interface Supplier<T> {
        /**
         * Get t.
         *
         * @return the t
         * @throws MinIoObjectException the min io object exception
         */
        T get() throws MinIoObjectException;
    }
}