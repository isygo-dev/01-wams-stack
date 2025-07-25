package eu.isygoit.storage.s3.api;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.s3.config.S3Config;
import eu.isygoit.storage.s3.object.FileStorage;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.List;
import java.util.Map;

/**
 * The interface Iawss 3 api service.
 */
public interface IAWSS3ApiService {

    /**
     * Gets connection.
     *
     * @param config the config
     * @return the connection
     */
    S3Client getConnection(S3Config config);

    /**
     * Update connection.
     *
     * @param config the config
     */
    void updateConnection(S3Config config);

    /**
     * Bucket exists boolean.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @return the boolean
     */
    boolean bucketExists(S3Config config, String bucketName);

    /**
     * Sets versioning bucket.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param status     the status
     */
    void setVersioningBucket(S3Config config, String bucketName, boolean status);

    /**
     * Make bucket.
     *
     * @param config     the config
     * @param bucketName the bucket name
     */
    void makeBucket(S3Config config, String bucketName);

    /**
     * Upload file.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param path       the path
     * @param objectName the object name
     * @param file       the multipart file
     * @param tags       the tags
     */
    void uploadFile(S3Config config, String bucketName, String path, String objectName,
                    MultipartFile file, Map<String, String> tags);

    /**
     * Get object byte [ ].
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param objectName the object name
     * @param versionID  the version id
     * @return the byte [ ]
     */
    byte[] getObject(S3Config config, String bucketName, String objectName, String versionID);

    /**
     * Gets presigned object url.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param objectName the object name
     * @return the presigned object url
     */
    String getPresignedObjectUrl(S3Config config, String bucketName, String objectName);

    /**
     * Delete object.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param objectName the object name
     */
    void deleteObject(S3Config config, String bucketName, String objectName);

    /**
     * Gets object by tags.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param tags       the tags
     * @param condition  the condition
     * @return the object by tags
     */
    List<FileStorage> getObjectByTags(S3Config config, String bucketName,
                                      Map<String, String> tags, IEnumLogicalOperator.Types condition);

    /**
     * Gets objects.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @return the objects
     */
    List<FileStorage> getObjects(S3Config config, String bucketName);

    /**
     * Update tags.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param objectName the object name
     * @param tags       the tags
     */
    void updateTags(S3Config config, String bucketName, String objectName, Map<String, String> tags);

    /**
     * Delete objects.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param objects    the objects
     */
    public void deleteObjects(S3Config config, String bucketName, List<DeleteObjectRequest> objects);

    /**
     * Delete bucket.
     *
     * @param config     the config
     * @param bucketName the bucket name
     */
    void deleteBucket(S3Config config, String bucketName);

    /**
     * Gets buckets.
     *
     * @param config the config
     * @return the buckets
     */
    List<Bucket> getBuckets(S3Config config);
}