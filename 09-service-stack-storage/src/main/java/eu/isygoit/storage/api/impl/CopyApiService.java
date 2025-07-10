package eu.isygoit.storage.api.impl;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.api.ICopyApiService;
import eu.isygoit.storage.exception.CopyObjectException;
import eu.isygoit.storage.object.Bucket;
import eu.isygoit.storage.object.FileStorage;
import eu.isygoit.storage.object.StorageConfig;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service implementation for Copy cloud storage operations with enhanced error handling,
 * connection pooling, and retry logic.
 */
@Slf4j
public abstract class CopyApiService implements ICopyApiService {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int DEFAULT_PRESIGNED_URL_EXPIRY_HOURS = 2;

    private final RestTemplate restTemplate;
    private final Map<String, StorageConfig> configMap;

    /**
     * Instantiates a new Copy api service.
     *
     * @param restTemplate the rest template
     * @param configMap    the config map
     */
    public CopyApiService(RestTemplate restTemplate, Map<String, StorageConfig> configMap) {
        this.restTemplate = restTemplate;
        this.configMap = configMap;
    }

    /**
     * Retrieves or stores the Copy configuration for the specified tenant.
     *
     * @param config Storage configuration containing tenant, credentials, and endpoint
     * @throws IllegalArgumentException if config is invalid
     */
    @Override
    public StorageConfig getConnection(StorageConfig config) {
        validateConfig(config);
        return configMap.computeIfAbsent(config.getTenant(), k -> config);
    }

    /**
     * Updates the Copy configuration for a tenant.
     *
     * @param config Storage configuration
     * @throws IllegalArgumentException if config is invalid
     */
    @Override
    public void updateConnection(StorageConfig config) {
        validateConfig(config);
        configMap.put(config.getTenant(), config);
        log.info("Updated Copy configuration for tenant: {}", config.getTenant());
    }

    /**
     * Checks if a bucket exists with retry logic.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @return true if bucket exists
     * @throws CopyObjectException on failure
     */
    @Override
    public boolean bucketExists(StorageConfig config, String bucketName) {
        validateBucketName(bucketName);
        return executeWithRetry(() -> {
            try {
                StorageConfig storedConfig = getConnection(config);
                HttpHeaders headers = createAuthHeaders(storedConfig);
                ResponseEntity<String> response = restTemplate.exchange(
                        storedConfig.getUrl() + "/buckets/" + bucketName,
                        HttpMethod.HEAD,
                        new HttpEntity<>(headers),
                        String.class);
                return response.getStatusCode() == HttpStatus.OK;
            } catch (Exception e) {
                throw new CopyObjectException("Error checking bucket existence", e);
            }
        });
    }

    /**
     * Enables or suspends bucket versioning.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param status     true to enable, false to suspend
     * @throws CopyObjectException on failure
     */
    @Override
    public void setVersioningBucket(StorageConfig config, String bucketName, boolean status) {
        validateBucketName(bucketName);
        executeWithRetry(() -> {
            try {
                StorageConfig storedConfig = getConnection(config);
                HttpHeaders headers = createAuthHeaders(storedConfig);
                Map<String, String> versioningConfig = new HashMap<>();
                versioningConfig.put("status", status ? "enabled" : "suspended");
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(versioningConfig, headers);
                restTemplate.put(storedConfig.getUrl() + "/buckets/" + bucketName + "/versioning", entity);
                log.info("Set versioning {} for bucket: {}", status ? "enabled" : "suspended", bucketName);
            } catch (Exception e) {
                throw new CopyObjectException("Error setting bucket versioning", e);
            }
            return null;
        });
    }

    /**
     * Creates a bucket if it doesn't exist.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @throws CopyObjectException on failure
     */
    @Override
    public void makeBucket(StorageConfig config, String bucketName) {
        validateBucketName(bucketName);
        executeWithRetry(() -> {
            try {
                if (!bucketExists(config, bucketName)) {
                    StorageConfig storedConfig = getConnection(config);
                    HttpHeaders headers = createAuthHeaders(storedConfig);
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    restTemplate.postForEntity(storedConfig.getUrl() + "/buckets/" + bucketName, entity, String.class);
                    log.info("Created bucket: {}", bucketName);
                }
            } catch (Exception e) {
                throw new CopyObjectException("Error creating bucket: " + bucketName, e);
            }
            return null;
        });
    }

    /**
     * Uploads a file to Copy with tags.
     *
     * @param config        Storage configuration
     * @param bucketName    Name of the bucket
     * @param path          Object path
     * @param objectName    Object name
     * @param multipartFile File to upload
     * @param tags          Metadata tags
     * @throws CopyObjectException on failure
     */
    @Override
    public void uploadFile(StorageConfig config, String bucketName, String path, String objectName,
                           MultipartFile multipartFile, Map<String, String> tags) {
        validateUploadParams(bucketName, path, objectName, multipartFile);
        executeWithRetry(() -> {
            try {
                makeBucket(config, bucketName);
                StorageConfig storedConfig = getConnection(config);
                HttpHeaders headers = createAuthHeaders(storedConfig);
                headers.setContentType(MediaType.valueOf(multipartFile.getContentType()));
                headers.add("x-copy-tags", encodeTags(tags));
                String fullPath = StringUtils.hasText(path) ? path + "/" + objectName : objectName;
                HttpEntity<byte[]> entity = new HttpEntity<>(multipartFile.getBytes(), headers);
                restTemplate.put(storedConfig.getUrl() + "/buckets/"
                        + bucketName + "/objects/" + fullPath, entity);
                log.info("Uploaded file: {} to bucket: {}", fullPath, bucketName);
            } catch (Exception e) {
                throw new CopyObjectException("Error uploading file: " + objectName, e);
            }
            return null;
        });
    }

    /**
     * Retrieves an object from Copy.
     *
     * @param config     Storage configuration
     *                   * @param bucketName Name of the bucket
     * @param objectName Object name
     * @param versionID  Version ID (optional)
     * @return Object content as byte array
     * @throws CopyObjectException on failure
     */
    @Override
    public byte[] getObject(StorageConfig config, String bucketName, String objectName, String versionID) {
        validateObjectParams(bucketName, objectName);
        return executeWithRetry(() -> {
            try {
                StorageConfig storedConfig = getConnection(config);
                HttpHeaders headers = createAuthHeaders(storedConfig);
                String url = storedConfig.getUrl() + "/buckets/" + bucketName + "/objects/" + objectName;
                if (StringUtils.hasText(versionID)) {
                    url += "?versionId=" + versionID;
                }
                ResponseEntity<byte[]> response = restTemplate.exchange(
                        url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
                return response.getBody();
            } catch (Exception e) {
                throw new CopyObjectException("Error retrieving object: " + objectName, e);
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
     * @throws CopyObjectException on failure
     */
    @Override
    public String getPresignedObjectUrl(StorageConfig config, String bucketName, String objectName) {
        validateObjectParams(bucketName, objectName);
        return executeWithRetry(() -> {
            try {
                StorageConfig storedConfig = getConnection(config);
                HttpHeaders headers = createAuthHeaders(storedConfig);
                Map<String, String> params = new HashMap<>();
                params.put("expires", String.valueOf(DEFAULT_PRESIGNED_URL_EXPIRY_HOURS * 3600));
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(
                        storedConfig.getUrl() + "/buckets/" + bucketName + "/objects/" + objectName + "/presigned",
                        entity, String.class);
                return response.getBody();
            } catch (Exception e) {
                throw new CopyObjectException("Error generating presigned URL for: " + objectName, e);
            }
        });
    }

    /**
     * Deletes an object from Copy.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @throws CopyObjectException on failure
     */
    @Override
    public void deleteObject(StorageConfig config, String bucketName, String objectName) {
        validateObjectParams(bucketName, objectName);
        executeWithRetry(() -> {
            try {
                StorageConfig storedConfig = getConnection(config);
                HttpHeaders headers = createAuthHeaders(storedConfig);
                restTemplate.exchange(
                        storedConfig.getUrl() + "/buckets/" + bucketName + "/objects/" + objectName,
                        HttpMethod.DELETE,
                        new HttpEntity<>(headers),
                        Void.class);
                log.info("Deleted object: {} from bucket: {}", objectName, bucketName);
            } catch (Exception e) {
                throw new CopyObjectException("Error deleting object: " + objectName, e);
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
     * @throws CopyObjectException on failure
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
                StorageConfig storedConfig = getConnection(config);
                HttpHeaders headers = createAuthHeaders(storedConfig);

                for (FileStorage object : allObjects) {
                    ResponseEntity<Map> response = restTemplate.exchange(
                            storedConfig.getUrl() + "/buckets/" + bucketName + "/objects/" + object.objectName + "/tags",
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            Map.class);
                    Map<String, String> objectTags = response.getBody();

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
                throw new CopyObjectException("Error retrieving objects by tags", e);
            }
        });
    }

    /**
     * Lists all objects in a bucket.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @return List of FileStorage objects
     * @throws CopyObjectException on failure
     */
    @Override
    public List<FileStorage> getObjects(StorageConfig config, String bucketName) {
        validateBucketName(bucketName);
        return executeWithRetry(() -> {
            try {
                StorageConfig storedConfig = getConnection(config);
                HttpHeaders headers = createAuthHeaders(storedConfig);
                ResponseEntity<List> response = restTemplate.exchange(
                        storedConfig.getUrl() + "/buckets/" + bucketName + "/objects?includeVersions=true&recursive=true",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        List.class);
                List<Map<String, Object>> items = response.getBody();
                List<FileStorage> listFileStorage = new ArrayList<>();
                for (Map<String, Object> item : items) {
                    FileStorage fileObject = new FileStorage();
                    fileObject.objectName = (String) item.get("objectName");
                    fileObject.size = ((Number) item.get("size")).longValue();
                    fileObject.etag = (String) item.get("etag");
                    fileObject.lastModified = parseDate((String) item.get("lastModified"));
                    fileObject.versionID = (String) item.get("versionId");
                    fileObject.currentVersion = (Boolean) item.get("isLatest");
                    listFileStorage.add(fileObject);
                }
                return listFileStorage;
            } catch (Exception e) {
                throw new CopyObjectException("Error listing objects in bucket: " + bucketName, e);
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
     * @throws CopyObjectException on failure
     */
    @Override
    public void updateTags(StorageConfig config, String bucketName, String objectName, Map<String, String> tags) {
        validateObjectParams(bucketName, objectName);
        if (tags == null) {
            throw new IllegalArgumentException("Tags cannot be null");
        }
        executeWithRetry(() -> {
            try {
                StorageConfig storedConfig = getConnection(config);
                HttpHeaders headers = createAuthHeaders(storedConfig);
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(tags, headers);
                restTemplate.put(storedConfig.getUrl() + "/buckets/" + bucketName + "/objects/" + objectName + "/tags", entity);
                log.info("Updated tags for object: {} in bucket: {}", objectName, bucketName);
            } catch (Exception e) {
                throw new CopyObjectException("Error updating tags for object: " + objectName, e);
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
     * @throws CopyObjectException on failure
     */
    @Override
    public void deleteObjects(StorageConfig config, String bucketName, List<DeleteObject> objects) {
        validateBucketName(bucketName);
        if (objects == null || objects.isEmpty()) {
            throw new IllegalArgumentException("Objects list cannot be null or empty");
        }
        executeWithRetry(() -> {
            try {
                StorageConfig storedConfig = getConnection(config);
                HttpHeaders headers = createAuthHeaders(storedConfig);
                HttpEntity<List<DeleteObject>> entity = new HttpEntity<>(objects, headers);
                ResponseEntity<List> response = restTemplate.exchange(
                        storedConfig.getUrl() + "/buckets/" + bucketName + "/objects/batch",
                        HttpMethod.DELETE,
                        entity,
                        List.class);
                List<Map<String, String>> errors = response.getBody();
                if (errors != null && !errors.isEmpty()) {
                    log.error("Errors occurred while deleting objects: {}", errors);
                    throw new CopyObjectException("Failed to delete some objects");
                }
                log.info("Deleted {} objects from bucket: {}", objects.size(), bucketName);
            } catch (Exception e) {
                throw new CopyObjectException("Error deleting objects from bucket: " + bucketName, e);
            }
            return null;
        });
    }

    /**
     * Deletes a bucket if it exists.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @throws CopyObjectException on failure
     */
    @Override
    public void deleteBucket(StorageConfig config, String bucketName) {
        validateBucketName(bucketName);
        executeWithRetry(() -> {
            try {
                if (bucketExists(config, bucketName)) {
                    StorageConfig storedConfig = getConnection(config);
                    HttpHeaders headers = createAuthHeaders(storedConfig);
                    restTemplate.exchange(
                            storedConfig.getUrl() + "/buckets/" + bucketName,
                            HttpMethod.DELETE,
                            new HttpEntity<>(headers),
                            Void.class);
                    log.info("Deleted bucket: {}", bucketName);
                }
            } catch (Exception e) {
                throw new CopyObjectException("Error deleting bucket: " + bucketName, e);
            }
            return null;
        });
    }

    /**
     * Lists all buckets for the given configuration.
     *
     * @param config Storage configuration
     * @return List of buckets
     * @throws CopyObjectException on failure
     */
    @Override
    public List<Bucket> getBuckets(StorageConfig config) {
        validateConfig(config);
        return executeWithRetry(() -> {
            try {
                StorageConfig storedConfig = getConnection(config);
                HttpHeaders headers = createAuthHeaders(storedConfig);
                ResponseEntity<List> response = restTemplate.exchange(
                        storedConfig.getUrl() + "/buckets",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        List.class);
                List<Map<String, Object>> bucketData = response.getBody();
                List<Bucket> buckets = new ArrayList<>();
                for (Map<String, Object> data : bucketData) {
                    buckets.add(new Bucket((String) data.get("name"), parseDate((String) data.get("creationDate"))));
                }
                log.info("Retrieved {} buckets for tenant: {}", buckets.size(), config.getTenant());
                return buckets;
            } catch (Exception e) {
                throw new CopyObjectException("Error listing buckets", e);
            }
        });
    }

    /**
     * Creates authentication headers for Copy API requests.
     *
     * @param config Storage configuration
     * @return HttpHeaders with authentication
     */
    private HttpHeaders createAuthHeaders(StorageConfig config) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(config.getUserName(), config.getPassword());
        return headers;
    }

    /**
     * Encodes tags for HTTP headers.
     *
     * @param tags Map of tags
     * @return Encoded tags string
     */
    private String encodeTags(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return tags.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
    }

    /**
     * Parses date string to ZonedDateTime.
     *
     * @param dateStr Date string
     * @return ZonedDateTime or null if parsing fails
     */
    private ZonedDateTime parseDate(String dateStr) {
        try {
            return ZonedDateTime.parse(dateStr);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr, e);
            return null;
        }
    }

    /**
     * Executes an operation with retry logic.
     *
     * @param operation The operation to execute
     * @param <T>       Return type
     * @return Operation result
     * @throws CopyObjectException on failure after retries
     */
    private <T> T executeWithRetry(Supplier<T> operation) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (CopyObjectException e) {
                attempt++;
                if (attempt == MAX_RETRIES) {
                    throw e;
                }
                try {
                    Thread.sleep((long) RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new CopyObjectException("Retry interrupted", ie);
                }
                log.warn("Retrying operation, attempt {}/{}", attempt, MAX_RETRIES);
            }
        }
        throw new CopyObjectException("Operation failed after maximum retries");
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
         * @throws CopyObjectException the copy object exception
         */
        T get() throws CopyObjectException;
    }
}