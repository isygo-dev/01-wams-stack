package eu.isygoit.storage.api.impl;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.api.IOpenIOApiService;
import eu.isygoit.storage.exception.OpenIOObjectException;
import eu.isygoit.storage.object.FileStorage;
import eu.isygoit.storage.object.StorageConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service implementation for OpenIO object storage operations using REST API.
 */
@Slf4j
public abstract class OpenIOApiService implements IOpenIOApiService {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int DEFAULT_PRESIGNED_URL_EXPIRY_HOURS = 2;

    private final Map<String, Object> openIoConnections;
    private final OkHttpClient httpClient;

    /**
     * Instantiates a new Open io api service.
     *
     * @param openIoConnections the open io connections
     * @param httpClient        the http client
     */
    public OpenIOApiService(Map<String, Object> openIoConnections, OkHttpClient httpClient) {
        this.openIoConnections = openIoConnections;
        this.httpClient = httpClient;
    }

    /**
     * Retrieves or creates an OpenIO client connection for the specified tenant (simulated via REST).
     *
     * @param config Storage configuration containing tenant, credentials, namespace, and endpoint
     * @return Simulated client instance (OkHttpClient for REST)
     * @throws OpenIOObjectException if connection creation fails
     */
    @Override
    public Object getConnection(StorageConfig config) {
        validateConfig(config);
        return openIoConnections.computeIfAbsent(config.getTenant(), k -> {
            try {
                return httpClient; // Use OkHttpClient for REST API calls
            } catch (Exception e) {
                log.error("Failed to create OpenIO connection for tenant: {}", config.getTenant(), e);
                throw new OpenIOObjectException("Failed to initialize OpenIO connection", e);
            }
        });
    }

    /**
     * Updates the OpenIO client connection for a tenant.
     *
     * @param config Storage configuration
     * @throws OpenIOObjectException if connection update fails
     */
    @Override
    public void updateConnection(StorageConfig config) {
        validateConfig(config);
        try {
            openIoConnections.put(config.getTenant(), httpClient);
            log.info("Updated OpenIO connection for tenant: {}", config.getTenant());
        } catch (Exception e) {
            log.error("Failed to update OpenIO connection for tenant: {}", config.getTenant(), e);
            throw new OpenIOObjectException("Failed to update OpenIO connection", e);
        }
    }

    /**
     * Checks if a container exists with retry logic.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @return true if container exists
     * @throws OpenIOObjectException on failure
     */
    @Override
    public boolean containerExists(StorageConfig config, String containerName) {
        validateContainerName(containerName);
        return executeWithRetry(() -> {
            try {
                Request request = new Request.Builder()
                        .url(config.getUrl() + "/v3.0/" + config.getNamespace() + "/container/show?acct=" + config.getTenant() + "&ref=" + containerName)
                        .header("X-Auth-Token", getAuthToken(config))
                        .get()
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    return response.isSuccessful();
                }
            } catch (IOException e) {
                throw new OpenIOObjectException("Error checking container existence", e);
            }
        });
    }

    /**
     * Enables or suspends container versioning.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param status        true to enable, false to suspend
     * @throws OpenIOObjectException if operation fails
     */
    @Override
    public void setVersioningContainer(StorageConfig config, String containerName, boolean status) {
        validateContainerName(containerName);
        executeWithRetry(() -> {
            try {
                String versioning = status ? "Enabled" : "Suspended";
                FormBody body = new FormBody.Builder()
                        .add("versioning", versioning)
                        .build();
                Request request = new Request.Builder()
                        .url(config.getUrl() + "/v3.0/" + config.getNamespace() + "/container/set?acct=" + config.getTenant() + "&ref=" + containerName)
                        .header("X-Auth-Token", getAuthToken(config))
                        .post(body)
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Failed to set versioning: " + response.message());
                    }
                    log.info("Set versioning {} for container: {}", versioning, containerName);
                }
            } catch (IOException e) {
                throw new OpenIOObjectException("Error setting container versioning", e);
            }
            return null;
        });
    }

    /**
     * Creates a container if it doesn't exist.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @throws OpenIOObjectException if container creation fails
     */
    @Override
    public void makeContainer(StorageConfig config, String containerName) {
        validateContainerName(containerName);
        executeWithRetry(() -> {
            try {
                if (!containerExists(config, containerName)) {
                    Request request = new Request.Builder()
                            .url(config.getUrl() + "/v3.0/" + config.getNamespace() + "/container/create?acct=" + config.getTenant() + "&ref=" + containerName)
                            .header("X-Auth-Token", getAuthToken(config))
                            .post(new FormBody.Builder().build())
                            .build();
                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            throw new IOException("Failed to create container: " + response.message());
                        }
                        log.info("Created container: {}", containerName);
                    }
                }
            } catch (IOException e) {
                throw new OpenIOObjectException("Error creating container: " + containerName, e);
            }
            return null;
        });
    }

    /**
     * Uploads a file to OpenIO with metadata.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param path          Object path
     * @param objectName    Object name
     * @param multipartFile File to upload
     * @param metadata      Metadata key-value pairs
     * @throws OpenIOObjectException if upload fails
     */
    @Override
    public void uploadFile(StorageConfig config, String containerName, String path, String objectName,
                           MultipartFile multipartFile, Map<String, String> metadata) {
        validateUploadParams(containerName, path, objectName, multipartFile);
        executeWithRetry(() -> {
            try {
                makeContainer(config, containerName);
                String fullPath = StringUtils.hasText(path) ? path + "/" + objectName : objectName;
                Request.Builder requestBuilder = new Request.Builder()
                        .url(config.getUrl() + "/v3.0/" + config.getNamespace() + "/content/create?acct=" + config.getTenant() + "&ref=" + containerName + "&path=" + fullPath)
                        .header("X-Auth-Token", getAuthToken(config))
                        .header("Content-Type", multipartFile.getContentType());
                if (metadata != null) {
                    metadata.forEach((key, value) -> requestBuilder.header("x-object-meta-" + key, value));
                }
                RequestBody requestBody = RequestBody.create(multipartFile.getBytes(), MediaType.parse(multipartFile.getContentType()));
                requestBuilder.post(requestBody);
                try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Failed to upload file: " + response.message());
                    }
                    log.info("Uploaded file: {} to container: {}", fullPath, containerName);
                }
            } catch (IOException e) {
                throw new OpenIOObjectException("Error uploading file: " + objectName, e);
            }
            return null;
        });
    }

    /**
     * Retrieves an object from OpenIO.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param objectName    Object name
     * @param versionID     Version ID (optional)
     * @return Object content as byte array
     * @throws OpenIOObjectException if retrieval fails
     */
    @Override
    public byte[] getObject(StorageConfig config, String containerName, String objectName, String versionID) {
        validateObjectParams(containerName, objectName);
        return executeWithRetry(() -> {
            try {
                String url = config.getUrl() + "/v3.0/" + config.getNamespace() + "/content/get?acct=" + config.getTenant() + "&ref=" + containerName + "&path=" + objectName;
                if (StringUtils.hasText(versionID)) {
                    url += "&version=" + versionID;
                }
                Request request = new Request.Builder()
                        .url(url)
                        .header("X-Auth-Token", getAuthToken(config))
                        .get()
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Failed to retrieve object: " + response.message());
                    }
                    return response.body().bytes();
                }
            } catch (IOException e) {
                throw new OpenIOObjectException("Error retrieving object: " + objectName, e);
            }
        });
    }

    /**
     * Generates a presigned URL for an object.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param objectName    Object name
     * @return Presigned URL
     * @throws OpenIOObjectException if URL generation fails
     */
    @Override
    public String getPresignedObjectUrl(StorageConfig config, String containerName, String objectName) {
        validateObjectParams(containerName, objectName);
        return executeWithRetry(() -> {
            try {
                String url = config.getUrl() + "/v3.0/" + config.getNamespace() + "/content/get?acct=" + config.getTenant() + "&ref=" + containerName + "&path=" + objectName;
                // OpenIO S3-compatible presigned URL generation (simplified, requires S3 API)
                // In practice, use AWS SDK for S3-compatible presigned URLs
                String presignedUrl = url + "&X-Amz-Expires=" + (DEFAULT_PRESIGNED_URL_EXPIRY_HOURS * 3600);
                return presignedUrl; // Placeholder: actual implementation requires S3 client
            } catch (Exception e) {
                throw new OpenIOObjectException("Error generating presigned URL for: " + objectName, e);
            }
        });
    }

    /**
     * Deletes an object from OpenIO.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param objectName    Object name
     * @throws OpenIOObjectException if deletion fails
     */
    @Override
    public void deleteObject(StorageConfig config, String containerName, String objectName) {
        validateObjectParams(containerName, objectName);
        executeWithRetry(() -> {
            try {
                Request request = new Request.Builder()
                        .url(config.getUrl() + "/v3.0/" + config.getNamespace() + "/content/delete?acct=" + config.getTenant() + "&ref=" + containerName + "&path=" + objectName)
                        .header("X-Auth-Token", getAuthToken(config))
                        .delete()
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Failed to delete object: " + response.message());
                    }
                    log.info("Deleted object: {} from container: {}", objectName, containerName);
                }
            } catch (IOException e) {
                throw new OpenIOObjectException("Error deleting object: " + objectName, e);
            }
            return null;
        });
    }

    /**
     * Retrieves objects by metadata with AND/OR condition.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param metadata      Metadata to filter
     * @param condition     Logical operator (AND/OR)
     * @return List of matching FileStorage objects
     * @throws OpenIOObjectException if retrieval fails
     */
    @Override
    public List<FileStorage> getObjectByMetadata(StorageConfig config, String containerName,
                                                 Map<String, String> metadata, IEnumLogicalOperator.Types condition) {
        validateContainerName(containerName);
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Metadata cannot be null or empty");
        }
        return executeWithRetry(() -> {
            try {
                List<FileStorage> listFileStorage = new ArrayList<>();
                List<FileStorage> allObjects = getObjects(config, containerName);
                for (FileStorage object : allObjects) {
                    Map<String, String> objectMetadata = getObjectMetadata(config, containerName, object.objectName);
                    boolean accepted = condition == IEnumLogicalOperator.Types.AND
                            ? objectMetadata.entrySet().containsAll(metadata.entrySet())
                            : !Collections.disjoint(objectMetadata.values(), metadata.values());
                    if (accepted) {
                        object.tags = objectMetadata.values().stream()
                                .filter(metadata.values()::contains)
                                .distinct()
                                .collect(Collectors.toList());
                        listFileStorage.add(object);
                    }
                }
                return listFileStorage;
            } catch (Exception e) {
                throw new OpenIOObjectException("Error retrieving objects by metadata", e);
            }
        });
    }

    /**
     * Lists all objects in a container.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @return List of FileStorage objects
     * @throws OpenIOObjectException if listing fails
     */
    @Override
    public List<FileStorage> getObjects(StorageConfig config, String containerName) {
        validateContainerName(containerName);
        return executeWithRetry(() -> {
            try {
                Request request = new Request.Builder()
                        .url(config.getUrl() + "/v3.0/" + config.getNamespace() + "/content/list?acct=" + config.getTenant() + "&ref=" + containerName)
                        .header("X-Auth-Token", getAuthToken(config))
                        .get()
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Failed to list objects: " + response.message());
                    }
                    // Parse JSON response (simplified, assumes JSON array of objects)
                    String json = response.body().string();
                    List<FileStorage> listFileStorage = parseObjectsFromJson(json);
                    return listFileStorage;
                }
            } catch (IOException e) {
                throw new OpenIOObjectException("Error listing objects in container: " + containerName, e);
            }
        });
    }

    /**
     * Updates metadata for an object.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param objectName    Object name
     * @param metadata      New metadata
     * @throws OpenIOObjectException if metadata update fails
     */
    @Override
    public void updateMetadata(StorageConfig config, String containerName, String objectName, Map<String, String> metadata) {
        validateObjectParams(containerName, objectName);
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata cannot be null");
        }
        executeWithRetry(() -> {
            try {
                Request.Builder requestBuilder = new Request.Builder()
                        .url(config.getUrl() + "/v3.0/" + config.getNamespace() + "/content/set_properties?acct=" + config.getTenant() + "&ref=" + containerName + "&path=" + objectName)
                        .header("X-Auth-Token", getAuthToken(config));
                metadata.forEach((key, value) -> requestBuilder.header("x-object-meta-" + key, value));
                Request request = requestBuilder.post(new FormBody.Builder().build()).build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Failed to update metadata: " + response.message());
                    }
                    log.info("Updated metadata for object: {} in container: {}", objectName, containerName);
                }
            } catch (IOException e) {
                throw new OpenIOObjectException("Error updating metadata for object: " + objectName, e);
            }
            return null;
        });
    }

    /**
     * Deletes multiple objects from a container.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param objects       List of object names to delete
     * @throws OpenIOObjectException if deletion fails
     */
    @Override
    public void deleteObjects(StorageConfig config, String containerName, List<String> objects) {
        validateContainerName(containerName);
        if (objects == null || objects.isEmpty()) {
            throw new IllegalArgumentException("Objects list cannot be null or empty");
        }
        executeWithRetry(() -> {
            try {
                for (String objectName : objects) {
                    deleteObject(config, containerName, objectName);
                }
                log.info("Deleted {} objects from container: {}", objects.size(), containerName);
            } catch (Exception e) {
                throw new OpenIOObjectException("Error deleting objects from container: " + containerName, e);
            }
            return null;
        });
    }

    /**
     * Deletes a container if it exists.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @throws OpenIOObjectException if container deletion fails
     */
    @Override
    public void deleteContainer(StorageConfig config, String containerName) {
        validateContainerName(containerName);
        executeWithRetry(() -> {
            try {
                if (containerExists(config, containerName)) {
                    Request request = new Request.Builder()
                            .url(config.getUrl() + "/v3.0/" + config.getNamespace() + "/container/destroy?acct=" + config.getTenant() + "&ref=" + containerName)
                            .header("X-Auth-Token", getAuthToken(config))
                            .delete()
                            .build();
                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            throw new IOException("Failed to delete container: " + response.message());
                        }
                        log.info("Deleted container: {}", containerName);
                    }
                }
            } catch (IOException e) {
                throw new OpenIOObjectException("Error deleting container: " + containerName, e);
            }
            return null;
        });
    }

    /**
     * Lists all containers for the given configuration.
     *
     * @param config Storage configuration
     * @return List of container names
     * @throws OpenIOObjectException if listing fails
     */
    @Override
    public List<String> getContainers(StorageConfig config) {
        validateConfig(config);
        return executeWithRetry(() -> {
            try {
                Request request = new Request.Builder()
                        .url(config.getUrl() + "/v3.0/" + config.getNamespace() + "/account/containers?acct=" + config.getTenant())
                        .header("X-Auth-Token", getAuthToken(config))
                        .get()
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Failed to list containers: " + response.message());
                    }
                    // Parse JSON response (simplified, assumes JSON array of container names)
                    String json = response.body().string();
                    return parseContainersFromJson(json);
                }
            } catch (IOException e) {
                throw new OpenIOObjectException("Error listing containers", e);
            }
        });
    }

    /**
     * Retrieves object metadata.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param objectName    Object name
     * @return Metadata key-value pairs
     * @throws OpenIOObjectException if retrieval fails
     */
    private Map<String, String> getObjectMetadata(StorageConfig config, String containerName, String objectName) {
        try {
            Request request = new Request.Builder()
                    .url(config.getUrl() + "/v3.0/" + config.getNamespace() + "/content/show?acct=" + config.getTenant() + "&ref=" + containerName + "&path=" + objectName)
                    .header("X-Auth-Token", getAuthToken(config))
                    .get()
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to retrieve metadata: " + response.message());
                }
                // Parse JSON response for metadata (simplified)
                String json = response.body().string();
                return parseMetadataFromJson(json);
            }
        } catch (IOException e) {
            throw new OpenIOObjectException("Error retrieving metadata for object: " + objectName, e);
        }
    }

    /**
     * Simulates retrieving an authentication token for OpenIO.
     *
     * @param config Storage configuration
     * @return Authentication token
     */
    private String getAuthToken(StorageConfig config) {
        // Placeholder: In practice, authenticate via OpenIO's auth endpoint or S3 credentials
        return "mock-auth-token";
    }

    /**
     * Parses objects from JSON response (simplified).
     *
     * @param json JSON response
     * @return List of FileStorage objects
     */
    private List<FileStorage> parseObjectsFromJson(String json) {
        // Placeholder: Parse JSON to extract object details (name, size, lastModified, etc.)
        List<FileStorage> objects = new ArrayList<>();
        FileStorage file = new FileStorage();
        file.objectName = "example-object";
        file.size = 1024;
        file.lastModified = ZonedDateTime.now();
        file.versionID = "1";
        file.currentVersion = true;
        objects.add(file);
        return objects;
    }

    /**
     * Parses containers from JSON response (simplified).
     *
     * @param json JSON response
     * @return List of container names
     */
    private List<String> parseContainersFromJson(String json) {
        // Placeholder: Parse JSON to extract container names
        return Arrays.asList("container1", "container2");
    }

    /**
     * Parses metadata from JSON response (simplified).
     *
     * @param json JSON response
     * @return Metadata key-value pairs
     */
    private Map<String, String> parseMetadataFromJson(String json) {
        // Placeholder: Parse JSON to extract metadata
        return new HashMap<>();
    }

    /**
     * Executes an operation with retry logic.
     *
     * @param operation The operation to execute
     * @param <T>       Return type
     * @return Operation result
     * @throws OpenIOObjectException on failure after retries
     */
    private <T> T executeWithRetry(Supplier<T> operation) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (OpenIOObjectException e) {
                attempt++;
                if (attempt == MAX_RETRIES) {
                    throw e;
                }
                try {
                    Thread.sleep((long) RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new OpenIOObjectException("Retry interrupted", ie);
                }
                log.warn("Retrying operation, attempt {}/{}", attempt, MAX_RETRIES);
            }
        }
        throw new OpenIOObjectException("Operation failed after maximum retries");
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
                !StringUtils.hasText(config.getPassword()) ||
                !StringUtils.hasText(config.getNamespace())) {
            throw new IllegalArgumentException("Invalid storage configuration");
        }
    }

    /**
     * Validates container name.
     *
     * @param containerName Name of the container
     * @throws IllegalArgumentException if invalid
     */
    private void validateContainerName(String containerName) {
        if (!StringUtils.hasText(containerName)) {
            throw new IllegalArgumentException("Container name cannot be empty");
        }
    }

    /**
     * Validates object parameters.
     *
     * @param containerName Name of the container
     * @param objectName    Object name
     * @throws IllegalArgumentException if invalid
     */
    private void validateObjectParams(String containerName, String objectName) {
        validateContainerName(containerName);
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("Object name cannot be empty");
        }
    }

    /**
     * Validates upload parameters.
     *
     * @param containerName Name of the container
     * @param path          Object path
     * @param objectName    Object name
     * @param multipartFile File to upload
     * @throws IllegalArgumentException if invalid
     */
    private void validateUploadParams(String containerName, String path, String objectName, MultipartFile multipartFile) {
        validateObjectParams(containerName, objectName);
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
         * @throws OpenIOObjectException the open io object exception
         */
        T get() throws OpenIOObjectException;
    }
}