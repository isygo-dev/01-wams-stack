package eu.isygoit.service;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.s3.config.S3Config;
import eu.isygoit.s3.object.FileStorage;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * The interface Object storage service.
 */
public interface IObjectStorageService {

    /**
     * Upload.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param path       the path
     * @param tags       the tags
     * @param file       the multipart file
     */
    void upload(S3Config config, String bucketName, String path, Map<String, String> tags, MultipartFile file);

    /**
     * Download byte [ ].
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param fileName   the file name
     * @param versionID  the version id
     * @return the byte [ ]
     */
    byte[] download(S3Config config, String bucketName, String fileName, String versionID);

    /**
     * Delete file.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param fileName   the file name
     */
    void deleteFile(S3Config config, String bucketName, String fileName);

    /**
     * Gets object by tags.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param tags       the tags
     * @param condition  the condition
     * @return the object by tags
     */
    List<FileStorage> getObjectByTags(S3Config config, String bucketName, Map<String, String> tags, IEnumLogicalOperator.Types condition);

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
     * Gets objects.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @return the objects
     */
    List<FileStorage> getObjects(S3Config config, String bucketName);

    /**
     * Delete objects.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param objects    the objects
     */
    void deleteObjects(S3Config config, String bucketName, List<DeleteObject> objects);

    /**
     * Save buckets.
     *
     * @param config     the config
     * @param bucketName the bucket name
     */
    void saveBuckets(S3Config config, String bucketName);

    /**
     * Sets versioning bucket.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param status     the status
     */
    void setVersioningBucket(S3Config config, String bucketName, boolean status);

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
