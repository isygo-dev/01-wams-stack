package eu.isygoit.storage.service;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.object.FileStorage;
import eu.isygoit.storage.object.StorageConfig;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * The interface Object storage api.
 */
public interface IObjectStorageService {

    /**
     * Upload.
     *
     * @param config        the config
     * @param bucketName    the bucket name
     * @param path          the path
     * @param tags          the tags
     * @param multipartFile the multipart file
     */
    void upload(StorageConfig config, String bucketName, String path, Map<String, String> tags, MultipartFile multipartFile);

    /**
     * Download byte [ ].
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param fileName   the file name
     * @param versionID  the version id
     * @return the byte [ ]
     */
    byte[] download(StorageConfig config, String bucketName, String fileName, String versionID);

    /**
     * Delete file.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param fileName   the file name
     */
    void deleteFile(StorageConfig config, String bucketName, String fileName);

    /**
     * Gets object by tags.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param tags       the tags
     * @param condition  the condition
     * @return the object by tags
     */
    List<FileStorage> getObjectByTags(StorageConfig config, String bucketName, Map<String, String> tags, IEnumLogicalOperator.Types condition);

    /**
     * Update tags.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param objectName the object name
     * @param tags       the tags
     */
    void updateTags(StorageConfig config, String bucketName, String objectName, Map<String, String> tags);

    /**
     * Gets objects.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @return the objects
     */
    List<FileStorage> getObjects(StorageConfig config, String bucketName);

    /**
     * Delete objects.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param objects    the objects
     */
    void deleteObjects(StorageConfig config, String bucketName, List<DeleteObject> objects);

    /**
     * Save buckets.
     *
     * @param config     the config
     * @param bucketName the bucket name
     */
    void saveBuckets(StorageConfig config, String bucketName);

    /**
     * Sets versioning bucket.
     *
     * @param config     the config
     * @param bucketName the bucket name
     * @param status     the status
     */
    void setVersioningBucket(StorageConfig config, String bucketName, boolean status);

    /**
     * Deletebucket.
     *
     * @param config     the config
     * @param bucketName the bucket name
     */
    void deleteBucket(StorageConfig config, String bucketName);

    /**
     * Gets buckets.
     *
     * @param config the config
     * @return the buckets
     */
    List<Bucket> getBuckets(StorageConfig config);
}
