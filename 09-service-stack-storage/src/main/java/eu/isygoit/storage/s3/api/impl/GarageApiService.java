package eu.isygoit.storage.s3.api.impl;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.exception.GarageObjectException;
import eu.isygoit.storage.s3.api.IGarageApiService;
import eu.isygoit.storage.s3.config.S3Config;
import eu.isygoit.storage.s3.object.FileStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Garage api service.
 */
@Slf4j
public abstract class GarageApiService implements IGarageApiService {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int DEFAULT_PRESIGNED_URL_EXPIRY_HOURS = 2;

    private final Map<String, S3Client> s3ClientMap;

    /**
     * Instantiates a new Garage api service.
     *
     * @param s3ClientMap the s 3 client map
     */
    public GarageApiService(Map<String, S3Client> s3ClientMap) {
        this.s3ClientMap = s3ClientMap;
    }

    @Override
    public S3Client getConnection(S3Config config) {
        validateConfig(config);
        return s3ClientMap.computeIfAbsent(config.getTenant(), k -> {
            try {
                log.info("Creating S3 client for tenant: {}, endpoint: {}", config.getTenant(), config.getUrl());
                return S3Client.builder()
                        .endpointOverride(URI.create(config.getUrl()))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(config.getUserName(), config.getPassword())))
                        .region(Region.of(config.getRegion() != null ? config.getRegion() : "us-east-1"))
                        .forcePathStyle(true)
                        .build();
            } catch (IllegalArgumentException e) {
                log.error("Invalid endpoint URL: {} for tenant: {}", config.getUrl(), config.getTenant(), e);
                throw new GarageObjectException("Invalid Ceph endpoint URL: " + config.getUrl(), e);
            } catch (Exception e) {
                if (e.getCause() instanceof UnknownHostException) {
                    log.error("DNS resolution failed for endpoint: {} for tenant: {}", config.getUrl(), config.getTenant(), e);
                    throw new GarageObjectException("Cannot resolve Ceph endpoint: " + config.getUrl(), e);
                }
                log.error("Failed to create S3 client for tenant: {}, endpoint: {}", config.getTenant(), config.getUrl(), e);
                throw new GarageObjectException("Failed to initialize S3 client", e);
            }
        });
    }

    @Override
    public void updateConnection(S3Config config) {
        validateConfig(config);
        try {
            S3Client client = S3Client.builder()
                    .endpointOverride(URI.create(config.getUrl()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(config.getUserName(), config.getPassword())))
                    .region(Region.of(config.getRegion() != null ? config.getRegion() : "us-east-1"))
                    .httpClient(ApacheHttpClient.builder().build())
                    .forcePathStyle(true)
                    .build();
            s3ClientMap.put(config.getTenant(), client);
            log.info("Updated S3 connection for tenant: {}", config.getTenant());
        } catch (Exception e) {
            log.error("Failed to update S3 connection for tenant: {}", config.getTenant(), e);
            throw new GarageObjectException("Failed to update S3 connection", e);
        }
    }

    @Override
    public boolean bucketExists(S3Config config, String bucketName) {
        validateBucketName(bucketName);
        return executeWithRetry(() -> {
            try {
                S3Client client = getConnection(config);
                client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
                return true;
            } catch (NoSuchBucketException e) {
                return false;
            } catch (Exception e) {
                throw new GarageObjectException("Error checking bucket existence", e);
            }
        });
    }

    @Override
    public void setVersioningBucket(S3Config config, String bucketName, boolean status) {
        validateBucketName(bucketName);
        executeWithRetry(() -> {
            try {
                S3Client client = getConnection(config);
                BucketVersioningStatus versioningStatus = status ? BucketVersioningStatus.ENABLED : BucketVersioningStatus.SUSPENDED;
                client.putBucketVersioning(PutBucketVersioningRequest.builder()
                        .bucket(bucketName)
                        .versioningConfiguration(VersioningConfiguration.builder().status(versioningStatus).build())
                        .build());
                log.info("Set versioning {} for bucket: {}", status ? "enabled" : "suspended", bucketName);
            } catch (Exception e) {
                throw new GarageObjectException("Error setting bucket versioning", e);
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
                    S3Client client = getConnection(config);
                    client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                    log.info("Created bucket: {}", bucketName);
                }
            } catch (Exception e) {
                throw new GarageObjectException("Error creating bucket: " + bucketName, e);
            }
            return null;
        });
    }

    @Override
    public void uploadFile(S3Config config, String bucketName, String path, String objectName,
                           MultipartFile multipartFile, Map<String, String> tags) {
        validateUploadParams(bucketName, path, objectName, multipartFile);
        executeWithRetry(() -> {
            try {
                makeBucket(config, bucketName);
                S3Client client = getConnection(config);
                String fullPath = StringUtils.hasText(path) ? path + "/" + objectName : objectName;
                List<Tag> s3Tags = tags != null ? tags.entrySet().stream()
                        .map(entry -> Tag.builder().key(entry.getKey()).value(entry.getValue()).build())
                        .collect(Collectors.toList()) : null;
                client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fullPath)
                        .contentType(multipartFile.getContentType())
                        .tagging(s3Tags != null ? Tagging.builder().tagSet(s3Tags).build() : null)
                        .build(), RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
                log.info("Uploaded file: {} to bucket: {}", fullPath, bucketName);
            } catch (Exception e) {
                throw new GarageObjectException("Error uploading file: " + objectName, e);
            }
            return null;
        });
    }

    @Override
    public byte[] getObject(S3Config config, String bucketName, String objectName, String versionID) {
        validateObjectParams(bucketName, objectName);
        return executeWithRetry(() -> {
            try {
                S3Client client = getConnection(config);
                GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectName);
                if (StringUtils.hasText(versionID)) {
                    requestBuilder.versionId(versionID);
                }
                try (var response = client.getObject(requestBuilder.build())) {
                    return IOUtils.toByteArray(response);
                }
            } catch (Exception e) {
                throw new GarageObjectException("Error retrieving object: " + objectName, e);
            }
        });
    }

    @Override
    public String getPresignedObjectUrl(S3Config config, String bucketName, String objectName) {
        validateObjectParams(bucketName, objectName);
        return executeWithRetry(() -> {
            try {
                S3Presigner presigner = S3Presigner.builder()
                        .endpointOverride(URI.create(config.getUrl()))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(config.getUserName(), config.getPassword())))
                        .region(Region.of(config.getRegion() != null ? config.getRegion() : "us-east-1"))
                        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                        .build();
                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofHours(DEFAULT_PRESIGNED_URL_EXPIRY_HOURS))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(objectName)
                                .build())
                        .build();
                PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
                String url = presignedRequest.url().toString();
                presigner.close();
                return url;
            } catch (Exception e) {
                throw new GarageObjectException("Error generating presigned URL for: " + objectName, e);
            }
        });
    }

    @Override
    public void deleteObject(S3Config config, String bucketName, String objectName) {
        validateObjectParams(bucketName, objectName);
        executeWithRetry(() -> {
            try {
                S3Client client = getConnection(config);
                client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectName)
                        .build());
                log.info("Deleted object: {} from bucket: {}", objectName, bucketName);
            } catch (Exception e) {
                throw new GarageObjectException("Error deleting object: " + objectName, e);
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
                S3Client client = getConnection(config);

                for (FileStorage object : allObjects) {
                    GetObjectTaggingResponse tagsResponse = client.getObjectTagging(GetObjectTaggingRequest.builder()
                            .bucket(bucketName)
                            .key(object.objectName)
                            .build());
                    Map<String, String> objectTags = tagsResponse.tagSet().stream()
                            .collect(Collectors.toMap(Tag::key, Tag::value));

                    boolean accepted = condition == IEnumLogicalOperator.Types.AND
                            ? objectTags.entrySet().containsAll(tags.entrySet())
                            : !Collections.disjoint(objectTags.values(), tags.values());

                    if (accepted) {
                        object.tags = objectTags.values().stream()
                                .filter(tags.values()::contains)
                                .distinct()
                                .collect(Collectors.toList());
                        listFileStorage.add(object);
                    }
                }
                return listFileStorage;
            } catch (Exception e) {
                throw new GarageObjectException("Error retrieving objects by tags", e);
            }
        });
    }

    @Override
    public List<FileStorage> getObjects(S3Config config, String bucketName) {
        validateBucketName(bucketName);
        return executeWithRetry(() -> {
            try {
                S3Client client = getConnection(config);
                ListObjectsV2Response response = client.listObjectsV2(ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .build());
                List<FileStorage> listFileStorage = new ArrayList<>();
                for (S3Object object : response.contents()) {
                    FileStorage fileObject = new FileStorage();
                    fileObject.objectName = object.key();
                    fileObject.size = object.size();
                    fileObject.etag = object.eTag();
                    fileObject.lastModified = object.lastModified().atZone(ZoneId.systemDefault());
                    // Garage may not return version IDs unless versioning is enabled
                    fileObject.versionID = null; // Versioning not supported in this simplified implementation
                    fileObject.currentVersion = true; // Simplified assumption
                    listFileStorage.add(fileObject);
                }
                return listFileStorage;
            } catch (Exception e) {
                throw new GarageObjectException("Error listing objects in bucket: " + bucketName, e);
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
                S3Client client = getConnection(config);
                List<Tag> s3Tags = tags.entrySet().stream()
                        .map(entry -> Tag.builder().key(entry.getKey()).value(entry.getValue()).build())
                        .collect(Collectors.toList());
                client.putObjectTagging(PutObjectTaggingRequest.builder()
                        .bucket(bucketName)
                        .key(objectName)
                        .tagging(Tagging.builder().tagSet(s3Tags).build())
                        .build());
                log.info("Updated tags for object: {} in bucket: {}", objectName, bucketName);
            } catch (Exception e) {
                throw new GarageObjectException("Error updating tags for object: " + objectName, e);
            }
            return null;
        });
    }

    @Override
    public void deleteObjects(S3Config config, String bucketName, List<DeleteObjectRequest> objects) {
        validateBucketName(bucketName);
        if (objects == null || objects.isEmpty()) {
            throw new IllegalArgumentException("Objects list cannot be null or empty");
        }
        executeWithRetry(() -> {
            try {
                S3Client client = getConnection(config);
                List<ObjectIdentifier> objectIdentifiers = objects.stream()
                        .map(req -> ObjectIdentifier.builder().key(req.key()).build())
                        .collect(Collectors.toList());
                DeleteObjectsResponse response = client.deleteObjects(DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(Delete.builder().objects(objectIdentifiers).build())
                        .build());
                if (!response.errors().isEmpty()) {
                    log.error("Errors occurred while deleting objects: {}", response.errors());
                    throw new GarageObjectException("Failed to delete some objects");
                }
                log.info("Deleted {} objects from bucket: {}", objects.size(), bucketName);
            } catch (Exception e) {
                throw new GarageObjectException("Error deleting objects from bucket: " + bucketName, e);
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
                    S3Client client = getConnection(config);
                    client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
                    log.info("Deleted bucket: {}", bucketName);
                }
            } catch (Exception e) {
                throw new GarageObjectException("Error deleting bucket: " + bucketName, e);
            }
            return null;
        });
    }

    @Override
    public List<Bucket> getBuckets(S3Config config) {
        validateConfig(config);
        return executeWithRetry(() -> {
            try {
                S3Client client = getConnection(config);
                ListBucketsResponse response = client.listBuckets();
                log.info("Retrieved {} buckets for tenant: {}", response.buckets().size(), config.getTenant());
                return response.buckets();
            } catch (Exception e) {
                throw new GarageObjectException("Error listing buckets", e);
            }
        });
    }

    private <T> T executeWithRetry(Supplier<T> operation) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (GarageObjectException e) {
                attempt++;
                if (attempt == MAX_RETRIES) {
                    throw e;
                }
                try {
                    Thread.sleep((long) RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new GarageObjectException("Retry interrupted", ie);
                }
                log.warn("Retrying operation, attempt {}/{}", attempt, MAX_RETRIES);
            }
        }
        throw new GarageObjectException("Operation failed after maximum retries");
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
         * @throws GarageObjectException the garage object exception
         */
        T get() throws GarageObjectException;
    }
}