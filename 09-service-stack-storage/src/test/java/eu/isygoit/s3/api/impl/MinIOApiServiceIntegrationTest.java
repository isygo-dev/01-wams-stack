package eu.isygoit.s3.api.impl;

import eu.isygoit.enums.IEnumStorage;
import eu.isygoit.s3.config.S3Config;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MinIOApiServiceIntegrationTest {

    @Container
    private static final MinIOContainer minioContainer = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
            .withUserName("minioadmin")
            .withPassword("minioadmin");
    private final Map<String, MinioClient> minIoMap = new HashMap<>();
    private MinIOApiService minIOApiService;
    private S3Config s3Config;

    @BeforeEach
    void setUp() {
        minIOApiService = new MinIOApiService(minIoMap);
        s3Config = S3Config.builder()
                .tenant("test-tenant")
                .type(IEnumStorage.Types.MINIO_STORAGE)
                .userName(minioContainer.getUserName())
                .password(minioContainer.getPassword())
                .url(minioContainer.getS3URL())
                .build();
    }

    @Test
    void testBucketOperations() {
        String bucketName = "test-bucket";

        // Create bucket
        minIOApiService.makeBucket(s3Config, bucketName);
        assertTrue(minIOApiService.bucketExists(s3Config, bucketName));

        // List buckets
        List<io.minio.messages.Bucket> buckets = minIOApiService.getBuckets(s3Config);
        assertTrue(buckets.stream().anyMatch(b -> b.name().equals(bucketName)));

        // Delete bucket
        minIOApiService.deleteBucket(s3Config, bucketName);
        assertFalse(minIOApiService.bucketExists(s3Config, bucketName));
    }

    @Test
    void testFileOperations() throws Exception {
        String bucketName = "file-test-bucket";
        String objectName = "test-file.txt";
        String content = "Hello, MinIO!";
        MockMultipartFile file = new MockMultipartFile("file", objectName, "text/plain", content.getBytes());

        minIOApiService.makeBucket(s3Config, bucketName);

        // Upload
        minIOApiService.uploadFile(s3Config, bucketName, "", objectName, file, Map.of("test-tag", "test-value"));

        // Get
        byte[] retrievedContent = minIOApiService.getObject(s3Config, bucketName, objectName, null);
        assertArrayEquals(content.getBytes(), retrievedContent);

        // Get Presigned URL
        String url = minIOApiService.getPresignedObjectUrl(s3Config, bucketName, objectName);
        assertNotNull(url);
        assertTrue(url.contains(bucketName));
        assertTrue(url.contains(objectName));

        // Delete
        minIOApiService.deleteObject(s3Config, bucketName, objectName);
        // Checking if object exists after deletion is tricky with current API without a dedicated exists method,
        // but getObject should fail or we can list objects.

        minIOApiService.deleteBucket(s3Config, bucketName);
    }
}
