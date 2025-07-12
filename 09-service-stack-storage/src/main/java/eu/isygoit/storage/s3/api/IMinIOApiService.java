package eu.isygoit.storage.s3.api;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.exception.MinIoObjectException;
import eu.isygoit.storage.s3.config.S3Config;
import eu.isygoit.storage.s3.object.FileStorage;
import io.minio.MinioClient;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Interface defining operations for interacting with MinIO object storage.
 */
public interface IMinIOApiService {

    /**
     * Retrieves or creates a MinIO client connection for the specified tenant.
     *
     * @param config Storage configuration containing tenant, credentials, and endpoint
     * @return MinioClient instance
     * @throws MinIoObjectException if connection creation fails
     */
    MinioClient getConnection(S3Config config);

    /**
     * Updates the MinIO client connection for a tenant.
     *
     * @param config Storage configuration
     * @throws MinIoObjectException if connection update fails
     */
    void updateConnection(S3Config config);

    /**
     * Checks if a bucket exists with retry logic.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @return true if bucket exists
     * @throws MinIoObjectException on failure
     */
    boolean bucketExists(S3Config config, String bucketName);

    /**
     * Enables or suspends bucket versioning.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param status     true to enable, false to suspend
     * @throws MinIoObjectException if operation fails
     */
    void setVersioningBucket(S3Config config, String bucketName, boolean status);

    /**
     * Creates a bucket if it doesn't exist.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @throws MinIoObjectException if bucket creation fails
     */
    void makeBucket(S3Config config, String bucketName);

    /**
     * Uploads a file to MinIO with optional tags.
     *
     * @param config        Storage configuration
     * @param bucketName    Name of the bucket
     * @param path          Object path
     * @param objectName    Object name
     * @param multipartFile File to upload
     * @param tags          Metadata tags
     * @throws MinIoObjectException if upload fails
     */
    void uploadFile(S3Config config, String bucketName, String path, String objectName,
                    MultipartFile multipartFile, Map<String, String> tags);

    /**
     * Retrieves an object from MinIO.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @param versionID  Version ID (optional)
     * @return Object content as byte array
     * @throws MinIoObjectException if retrieval fails
     */
    byte[] getObject(S3Config config, String bucketName, String objectName, String versionID);

    /**
     * Generates a presigned URL for an object.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @return Presigned URL
     * @throws MinIoObjectException if URL generation fails
     */
    String getPresignedObjectUrl(S3Config config, String bucketName, String objectName);

    /**
     * Deletes an object from MinIO.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @throws MinIoObjectException if deletion fails
     */
    void deleteObject(S3Config config, String bucketName, String objectName);

    /**
     * Retrieves objects by tags with AND/OR condition.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param tags       Tags to filter
     * @param condition  Logical operator (AND/OR)
     * @return List of matching FileStorage objects
     * @throws MinIoObjectException if retrieval fails
     */
    List<FileStorage> getObjectByTags(S3Config config, String bucketName,
                                      Map<String, String> tags, IEnumLogicalOperator.Types condition);

    /**
     * Lists all objects in a bucket.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @return List of FileStorage objects
     * @throws MinIoObjectException if listing fails
     */
    List<FileStorage> getObjects(S3Config config, String bucketName);

    /**
     * Updates tags for an object.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objectName Object name
     * @param tags       New tags
     * @throws MinIoObjectException if tag update fails
     */
    void updateTags(S3Config config, String bucketName, String objectName, Map<String, String> tags);

    /**
     * Deletes multiple objects from a bucket.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @param objects    List of objects to delete
     * @throws MinIoObjectException if deletion fails
     */
    void deleteObjects(S3Config config, String bucketName, List<DeleteObject> objects);

    /**
     * Deletes a bucket if it exists.
     *
     * @param config     Storage configuration
     * @param bucketName Name of the bucket
     * @throws MinIoObjectException if bucket deletion fails
     */
    void deleteBucket(S3Config config, String bucketName);

    /**
     * Lists all buckets for the given configuration.
     *
     * @param config Storage configuration
     * @return List of buckets
     * @throws MinIoObjectException if listing fails
     */
    List<Bucket> getBuckets(S3Config config);
}