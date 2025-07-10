package eu.isygoit.storage.api;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.exception.OpenIOObjectException;
import eu.isygoit.storage.object.FileStorage;
import eu.isygoit.storage.object.StorageConfig;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Interface defining operations for interacting with OpenIO object storage.
 */
public interface IOpenIOApiService {

    /**
     * Retrieves or creates an OpenIO client connection for the specified tenant.
     *
     * @param config Storage configuration containing tenant, credentials, namespace, and endpoint
     * @return OpenIO client instance (simulated as Object for REST-based approach)
     * @throws OpenIOObjectException if connection creation fails
     */
    Object getConnection(StorageConfig config);

    /**
     * Updates the OpenIO client connection for a tenant.
     *
     * @param config Storage configuration
     * @throws OpenIOObjectException if connection update fails
     */
    void updateConnection(StorageConfig config);

    /**
     * Checks if a container exists with retry logic.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @return true if container exists
     * @throws OpenIOObjectException on failure
     */
    boolean containerExists(StorageConfig config, String containerName);

    /**
     * Enables or suspends container versioning.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param status        true to enable, false to suspend
     * @throws OpenIOObjectException if operation fails
     */
    void setVersioningContainer(StorageConfig config, String containerName, boolean status);

    /**
     * Creates a container if it doesn't exist.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @throws OpenIOObjectException if container creation fails
     */
    void makeContainer(StorageConfig config, String containerName);

    /**
     * Uploads a file to OpenIO with optional metadata.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param path          Object path
     * @param objectName    Object name
     * @param multipartFile File to upload
     * @param metadata      Metadata key-value pairs
     * @throws OpenIOObjectException if upload fails
     */
    void uploadFile(StorageConfig config, String containerName, String path, String objectName,
                    MultipartFile multipartFile, Map<String, String> metadata);

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
    byte[] getObject(StorageConfig config, String containerName, String objectName, String versionID);

    /**
     * Generates a presigned URL for an object.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param objectName    Object name
     * @return Presigned URL
     * @throws OpenIOObjectException if URL generation fails
     */
    String getPresignedObjectUrl(StorageConfig config, String containerName, String objectName);

    /**
     * Deletes an object from OpenIO.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param objectName    Object name
     * @throws OpenIOObjectException if deletion fails
     */
    void deleteObject(StorageConfig config, String containerName, String objectName);

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
    List<FileStorage> getObjectByMetadata(StorageConfig config, String containerName,
                                          Map<String, String> metadata, IEnumLogicalOperator.Types condition);

    /**
     * Lists all objects in a container.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @return List of FileStorage objects
     * @throws OpenIOObjectException if listing fails
     */
    List<FileStorage> getObjects(StorageConfig config, String containerName);

    /**
     * Updates metadata for an object.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param objectName    Object name
     * @param metadata      New metadata
     * @throws OpenIOObjectException if metadata update fails
     */
    void updateMetadata(StorageConfig config, String containerName, String objectName, Map<String, String> metadata);

    /**
     * Deletes multiple objects from a container.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @param objects       List of object names to delete
     * @throws OpenIOObjectException if deletion fails
     */
    void deleteObjects(StorageConfig config, String containerName, List<String> objects);

    /**
     * Deletes a container if it exists.
     *
     * @param config        Storage configuration
     * @param containerName Name of the container
     * @throws OpenIOObjectException if container deletion fails
     */
    void deleteContainer(StorageConfig config, String containerName);

    /**
     * Lists all containers for the given configuration.
     *
     * @param config Storage configuration
     * @return List of container names
     * @throws OpenIOObjectException if listing fails
     */
    List<String> getContainers(StorageConfig config);
}