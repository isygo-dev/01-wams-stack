package eu.isygoit.storage.api;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.exception.VersityObjectException;
import eu.isygoit.storage.object.FileStorage;
import eu.isygoit.storage.object.StorageConfig;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.List;
import java.util.Map;

/**
 * Interface defining operations for interacting with Versity S3 Gateway object storage.
 */
public interface IVersityApiService {

    /**
     * Retrieves or creates an S3 client connection for the specified tenant.
     *
     * @param config Storage configuration containing tenant, credentials, and endpoint
     * @return S3Client instance
     * @throws VersityObjectException if connection creation fails
     */
    software.amazon.awssdk.services.s3.S3Client getConnection(StorageConfig config);

    /**
     * Updates the S3 client connection for a tenant.
     *
     * @param config Storage configuration
     * @throws VersityObjectException if connection update fails
     */
    void updateConnection(StorageConfig config);

    /**
     * Checks if a bucket exists with retry logic.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @return true if bucket exists
     * @throws VersityObjectException on failure
     */
    boolean bucketExists(StorageConfig config, String bucketName);

    /**
     * Enables or suspends bucket versioning.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param status     true to enable, false to suspend
     * @throws VersityObjectException if operation fails
     */
    void setVersioningBucket(StorageConfig config, String bucketName, boolean status);

    /**
     * Creates a bucket if it doesn't exist.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @throws VersityObjectException if bucket creation fails
     */
    void makeBucket(StorageConfig config, String bucketName);

    /**
     * Uploads a file to Versity Gateway with optional tags.
     *
     * @param config        Storage configuration
     * @param bucketName    Name of the bucket
     * @param path          Object path
     * @param objectName    Object name
     * @param multipartFile File to upload
     * @param tags          Metadata tags
     * @throws VersityObjectException if upload fails
     */
    void uploadFile(StorageConfig config, String bucketName, String path, String objectName,
                    MultipartFile multipartFile, Map<String, String> tags);

    /**
     * Retrieves an object from Versity Gateway.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @param versionID  Version ID (optional)
     * @return Object content as byte array
     * @throws VersityObjectException if retrieval fails
     */
    byte[] getObject(StorageConfig config, String bucketName, String objectName, String versionID);

    /**
     * Generates a presigned URL for an object.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @return Presigned URL
     * @throws VersityObjectException if URL generation fails
     */
    String getPresignedObjectUrl(StorageConfig config, String bucketName, String objectName);

    /**
     * Deletes an object from Versity Gateway.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @throws VersityObjectException if deletion fails
     */
    void deleteObject(StorageConfig config, String bucketName, String objectName);

    /**
     * Retrieves objects by tags with AND/OR condition.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param tags       Tags to filter
     * @param condition  Logical operator (AND/OR)
     * @return List of matching FileStorage objects
     * @throws VersityObjectException if retrieval fails
     */
    List<FileStorage> getObjectByTags(StorageConfig config, String bucketName,
                                      Map<String, String> tags, IEnumLogicalOperator.Types condition);

    /**
     * Lists all objects in a bucket.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @return List of FileStorage objects
     * @throws VersityObjectException if listing fails
     */
    List<FileStorage> getObjects(StorageConfig config, String bucketName);

    /**
     * Updates tags for an object.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @param tags       New tags
     * @throws VersityObjectException if tag update fails
     */
    void updateTags(StorageConfig config, String bucketName, String objectName, Map<String, String> tags);

    /**
     * Deletes multiple objects from a bucket.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objects    List of objects to delete
     * @throws VersityObjectException if deletion fails
     */
    void deleteObjects(StorageConfig config, String bucketName, List<DeleteObjectRequest> objects);

    /**
     * Deletes a bucket if it exists.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @throws VersityObjectException if bucket deletion fails
     */
    void deleteBucket(StorageConfig config, String bucketName);

    /**
     * Lists all buckets for the given configuration.
     *
     * @param config Storage configuration
     * @return List of buckets
     * @throws VersityObjectException if listing fails
     */
    List<Bucket> getBuckets(StorageConfig config);
}