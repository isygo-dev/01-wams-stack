package eu.isygoit.s3.api.impl;

import eu.isygoit.enums.IEnumStorage;
import eu.isygoit.s3.config.S3Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class S3BucketApiServiceIntegrationTest {

    @Container
    private static final MinIOContainer minioContainer = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
            .withUserName("minioadmin")
            .withPassword("minioadmin");

    private S3BucketApiService s3BucketApiService;
    private S3Config s3Config;
    private final Map<String, S3Client> s3ClientMap = new HashMap<>();

    @BeforeEach
    void setUp() {
        s3BucketApiService = new S3BucketApiService(s3ClientMap);
        s3Config = S3Config.builder()
                .tenant("test-tenant-s3")
                .type(IEnumStorage.Types.MINIO_STORAGE)
                .userName(minioContainer.getUserName())
                .password(minioContainer.getPassword())
                .url(minioContainer.getS3URL())
                .region("us-east-1") // MinIO default
                .build();
    }

    @Test
    void testBucketOperations() {
        String bucketName = "test-bucket-s3";

        // Create bucket
        s3BucketApiService.makeBucket(s3Config, bucketName);
        assertTrue(s3BucketApiService.bucketExists(s3Config, bucketName));

        // List buckets
        List<Bucket> buckets = s3BucketApiService.getBuckets(s3Config);
        assertTrue(buckets.stream().anyMatch(b -> b.name().equals(bucketName)));

        // Delete bucket
        s3BucketApiService.deleteBucket(s3Config, bucketName);
        assertFalse(s3BucketApiService.bucketExists(s3Config, bucketName));
    }

    @Test
    void testFileOperations() throws Exception {
        String bucketName = "file-test-bucket-s3";
        String objectName = "test-file-s3.txt";
        String content = "Hello, S3 via MinIO!";
        MockMultipartFile file = new MockMultipartFile("file", objectName, "text/plain", content.getBytes());

        s3BucketApiService.makeBucket(s3Config, bucketName);

        // Upload
        s3BucketApiService.uploadFile(s3Config, bucketName, "", objectName, file, Map.of("test-tag", "test-value"));

        // Get
        byte[] retrievedContent = s3BucketApiService.getObject(s3Config, bucketName, objectName, null);
        assertArrayEquals(content.getBytes(), retrievedContent);

        // Delete
        s3BucketApiService.deleteObject(s3Config, bucketName, objectName);
        
        s3BucketApiService.deleteBucket(s3Config, bucketName);
    }
}
