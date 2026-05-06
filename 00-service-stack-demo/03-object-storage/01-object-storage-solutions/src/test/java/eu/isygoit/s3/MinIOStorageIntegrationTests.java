package eu.isygoit.s3;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.exception.MinIoS3BucketException;
import eu.isygoit.s3.config.S3Config;
import eu.isygoit.s3.object.FileStorage;
import eu.isygoit.s3.object.MetaData;
import eu.isygoit.s3.service.MinIOService;
import io.minio.GetBucketVersioningArgs;
import io.minio.MinioClient;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import io.minio.messages.VersioningConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Min io storage application test.
 */
@ActiveProfiles("MinIO")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MinIOStorageIntegrationTests {

    @Container
    private static final MinIOContainer minioContainer = new MinIOContainer("minio/minio:latest")
            .withUserName("testuser")
            .withPassword("testpassword");
    private static final String BUCKET_NAME = "test-bucket";
    private static final String OBJECT_NAME = "test-object.txt";
    private static final String TENANT = "test-tenant";
    @Autowired
    private MinIOService minIOService;
    private S3Config s3Config;

    /**
     * Minio properties.
     *
     * @param registry the registry
     */
    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", minioContainer::getS3URL);
        registry.add("minio.username", minioContainer::getUserName);
        registry.add("minio.password", minioContainer::getPassword);
    }

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        s3Config = S3Config.builder()
                .tenant(TENANT)
                .url(minioContainer.getS3URL())
                .userName(minioContainer.getUserName())
                .password(minioContainer.getPassword())
                .build();
    }

    /**
     * Test create and check bucket.
     */
    @Test
    @Order(1)
    void testCreateAndCheckBucket() {
        minIOService.makeBucket(s3Config, BUCKET_NAME);
        assertTrue(minIOService.bucketExists(s3Config, BUCKET_NAME));
    }

    /**
     * Test set versioning.
     */
    @Test
    @Order(2)
    void testSetVersioning() {
        minIOService.makeBucket(s3Config, BUCKET_NAME);

        minIOService.setVersioningBucket(s3Config, BUCKET_NAME, true);
        MinioClient client = minIOService.getConnection(s3Config);
        try {
            VersioningConfiguration config = client.getBucketVersioning(
                    GetBucketVersioningArgs.builder().bucket(BUCKET_NAME).build());
            assertEquals(VersioningConfiguration.Status.ENABLED, config.status());
        } catch (Exception e) {
            fail("Failed to check versioning: " + e.getMessage());
        }

        minIOService.setVersioningBucket(s3Config, BUCKET_NAME, false);
        try {
            VersioningConfiguration config = client.getBucketVersioning(
                    GetBucketVersioningArgs.builder().bucket(BUCKET_NAME).build());
            assertEquals(VersioningConfiguration.Status.SUSPENDED, config.status());
        } catch (Exception e) {
            fail("Failed to check versioning: " + e.getMessage());
        }
    }

    /**
     * Test upload and get object.
     */
    @Test
    @Order(3)
    void testUploadAndGetObject() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Test content".getBytes(StandardCharsets.UTF_8));
        MetaData metaData = MetaData.builder()
                .bucketName(BUCKET_NAME)
                .objectName(OBJECT_NAME)
                .contentType(file.getContentType() != null ? file.getContentType() : "text/plain")
                .size(file.getSize())
                .etag("etag123")
                .tags(List.of("value1", "value2"))
                .tagsMap(Map.of("key1", "value1", "key2", "value2"))
                .build();
        minIOService.uploadFile(s3Config, metaData, file);
        byte[] data = minIOService.getObject(s3Config, BUCKET_NAME, OBJECT_NAME, null);
        assertEquals("Test content", new String(data, StandardCharsets.UTF_8));
    }

    /**
     * Test get presigned url.
     */
    @Test
    @Order(4)
    void testGetPresignedUrl() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Content for presigned".getBytes(StandardCharsets.UTF_8));
        MetaData metaData = MetaData.builder()
                .bucketName(BUCKET_NAME)
                .objectName(OBJECT_NAME)
                .contentType(file.getContentType() != null ? file.getContentType() : "text/plain")
                .size(file.getSize())
                .etag("etag123")
                .tags(List.of("value1", "value2"))
                .tagsMap(Map.of("key1", "value1", "key2", "value2"))
                .build();
        minIOService.uploadFile(s3Config, metaData, file);
        String url = minIOService.getPresignedObjectUrl(s3Config, BUCKET_NAME, OBJECT_NAME);
        assertNotNull(url);
        assertTrue(url.contains(OBJECT_NAME));
    }

    /**
     * Test get objects by tags.
     */
    @Test
    @Order(5)
    void testGetObjectsByTags() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Tagged content".getBytes(StandardCharsets.UTF_8));
        Map<String, String> tags = Map.of("type", "document", "env", "test");
        MetaData metaData = MetaData.builder()
                .bucketName(BUCKET_NAME)
                .objectName(OBJECT_NAME)
                .contentType(file.getContentType() != null ? file.getContentType() : "text/plain")
                .size(file.getSize())
                .etag("etag123")
                .tags(tags.values().stream().toList())
                .tagsMap(tags)
                .build();
        minIOService.uploadFile(s3Config, metaData, file);

        Map<String, String> searchTags = Map.of("type", "document");
        List<FileStorage> results = minIOService.getObjectByTags(
                s3Config, BUCKET_NAME, searchTags, IEnumLogicalOperator.Types.AND);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(f -> f.getObjectName().equals(OBJECT_NAME)));
    }

    /**
     * Test update tags.
     */
    @Test
    @Order(6)
    void testUpdateTags() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Content to tag".getBytes(StandardCharsets.UTF_8));
        MetaData metaData = MetaData.builder()
                .bucketName(BUCKET_NAME)
                .objectName(OBJECT_NAME)
                .contentType(file.getContentType() != null ? file.getContentType() : "text/plain")
                .size(file.getSize())
                .etag("etag123")
                .tags(List.of("value1", "value2"))
                .tagsMap(Map.of("key1", "value1", "key2", "value2"))
                .build();
        minIOService.uploadFile(s3Config, metaData, file);

        minIOService.updateTags(s3Config, BUCKET_NAME, OBJECT_NAME, Map.of("updated", "yes"));

        List<FileStorage> results = minIOService.getObjectByTags(
                s3Config, BUCKET_NAME, Map.of("updated", "yes"), IEnumLogicalOperator.Types.AND);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(f -> f.getObjectName().equals(OBJECT_NAME)));
    }

    /**
     * Test delete object.
     */
    @Test
    @Order(7)
    void testDeleteObject() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Content to delete".getBytes(StandardCharsets.UTF_8));
        MetaData metaData = MetaData.builder()
                .bucketName(BUCKET_NAME)
                .objectName(OBJECT_NAME)
                .contentType(file.getContentType() != null ? file.getContentType() : "text/plain")
                .size(file.getSize())
                .etag("etag123")
                .tags(List.of("value1", "value2"))
                .tagsMap(Map.of("key1", "value1", "key2", "value2"))
                .build();
        minIOService.uploadFile(s3Config, metaData, file);
        minIOService.deleteObject(s3Config, BUCKET_NAME, OBJECT_NAME);
        assertThrows(MinIoS3BucketException.class, () ->
                minIOService.getObject(s3Config, BUCKET_NAME, OBJECT_NAME, null));
    }

    /**
     * Test delete multiple objects.
     */
    @Test
    @Order(8)
    void testDeleteMultipleObjects() {
        MockMultipartFile f1 = new MockMultipartFile("f1", "file1.txt", "text/plain", "1".getBytes());
        MockMultipartFile f2 = new MockMultipartFile("f2", "file2.txt", "text/plain", "2".getBytes());

        MetaData metaData1 = MetaData.builder()
                .bucketName(BUCKET_NAME)
                .objectName(OBJECT_NAME)
                .contentType(f1.getContentType() != null ? f1.getContentType() : "text/plain")
                .size(f1.getSize())
                .etag("etag123")
                .tags(List.of("value1", "value2"))
                .tagsMap(Map.of("key1", "value1", "key2", "value2"))
                .build();
        minIOService.uploadFile(s3Config, metaData1, f1);

        MetaData metaData2 = MetaData.builder()
                .bucketName(BUCKET_NAME)
                .objectName(OBJECT_NAME)
                .contentType(f2.getContentType() != null ? f2.getContentType() : "text/plain")
                .size(f2.getSize())
                .etag("etag123")
                .tags(List.of("value1", "value2"))
                .tagsMap(Map.of("key1", "value1", "key2", "value2"))
                .build();
        minIOService.uploadFile(s3Config, metaData2, f2);

        minIOService.deleteObjects(s3Config, BUCKET_NAME,
                List.of(new DeleteObject("file1.txt"),
                        new DeleteObject("file2.txt")));

        assertThrows(MinIoS3BucketException.class, () ->
                minIOService.getObject(s3Config, BUCKET_NAME, "file1.txt", null));
        assertThrows(MinIoS3BucketException.class, () ->
                minIOService.getObject(s3Config, BUCKET_NAME, "file2.txt", null));
    }

    /**
     * Test list buckets.
     */
    @Test
    @Order(9)
    void testListBuckets() {
        List<Bucket> buckets = minIOService.getBuckets(s3Config);
        assertFalse(buckets.isEmpty());
        assertTrue(buckets.stream().anyMatch(b -> b.name().equals(BUCKET_NAME)));
    }

    /**
     * Test delete bucket.
     */
    @Test
    @Order(10)
    void testDeleteBucket() {
        minIOService.makeBucket(s3Config, "bucket-delete");
        minIOService.deleteBucket(s3Config, "bucket-delete");
        assertFalse(minIOService.bucketExists(s3Config, "bucket-delete"));
    }

    /**
     * Test invalid config.
     */
    @Test
    @Order(11)
    void testInvalidConfig() {
        S3Config invalid = new S3Config();
        assertThrows(IllegalArgumentException.class, () -> minIOService.getConnection(invalid));
    }

    /**
     * Test invalid bucket name.
     */
    @Test
    @Order(12)
    void testInvalidBucketName() {
        assertThrows(IllegalArgumentException.class, () -> minIOService.bucketExists(s3Config, ""));
    }

    /**
     * Test invalid object name.
     */
    @Test
    @Order(13)
    void testInvalidObjectName() {
        assertThrows(IllegalArgumentException.class,
                () -> minIOService.getObject(s3Config, BUCKET_NAME, "", null));
    }

    /**
     * Test invalid upload params.
     */
    @Test
    @Order(14)
    void testInvalidUploadParams() {
        MetaData metaData = MetaData.builder()
                .bucketName(BUCKET_NAME)
                .objectName(OBJECT_NAME)
                .etag("etag123")
                .tags(List.of("value1", "value2"))
                .tagsMap(Map.of("key1", "value1", "key2", "value2"))
                .build();
        assertThrows(IllegalArgumentException.class,
                () -> minIOService.uploadFile(s3Config, metaData, null));
    }

    /**
     * Test update connection.
     */
    @Test
    @Order(15)
    void testUpdateConnection() {
        assertDoesNotThrow(() -> minIOService.updateConnection(s3Config));
    }

    /**
     * Test upload with path.
     */
    @Test
    @Order(16)
    void testUploadWithPath() {
        String objectPath = "/dir1/dir2/";
        String objectName = "path-object.txt";
        MockMultipartFile file = new MockMultipartFile("file", objectPath, "text/plain",
                "Path content".getBytes(StandardCharsets.UTF_8));
        MetaData metaData = MetaData.builder()
                .bucketName(BUCKET_NAME)
                .objectName(objectName)
                .path(objectPath)
                .contentType(file.getContentType() != null ? file.getContentType() : "text/plain")
                .size(file.getSize())
                .etag("etag123")
                .tags(List.of("value1", "value2"))
                .tagsMap(Map.of("key1", "value1", "key2", "value2"))
                .build();
        minIOService.uploadFile(s3Config, metaData, file);
        byte[] retrieved = minIOService.getObject(s3Config, BUCKET_NAME, objectPath + objectName, null);
        assertEquals("Path content", new String(retrieved));
    }

    /**
     * Test list objects in bucket.
     */
    @Test
    @Order(17)
    void testListObjectsInBucket() {
        MockMultipartFile file = new MockMultipartFile("file", "list.txt", "text/plain", "list".getBytes());
        MetaData metaData = MetaData.builder()
                .bucketName(BUCKET_NAME)
                .objectName("list.txt")
                .contentType(file.getContentType() != null ? file.getContentType() : "text/plain")
                .size(file.getSize())
                .etag("etag123")
                .tags(List.of("value1", "value2"))
                .tagsMap(Map.of("key1", "value1", "key2", "value2"))
                .build();
        minIOService.uploadFile(s3Config, metaData, file);
        List<FileStorage> files = minIOService.getObjects(s3Config, BUCKET_NAME);
        assertTrue(files.stream().anyMatch(f -> f.getObjectName().equals("list.txt")));
    }

    /**
     * Test get object with empty version id.
     */
    @Test
    @Order(18)
    void testGetObjectWithEmptyVersionId() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "With empty version".getBytes());
        MetaData metaData = MetaData.builder()
                .bucketName(BUCKET_NAME)
                .objectName(OBJECT_NAME)
                .contentType(file.getContentType() != null ? file.getContentType() : "text/plain")
                .size(file.getSize())
                .etag("etag123")
                .tags(List.of("value1", "value2"))
                .tagsMap(Map.of("key1", "value1", "key2", "value2"))
                .build();
        minIOService.uploadFile(s3Config, metaData, file);
        assertDoesNotThrow(() ->
                minIOService.getObject(s3Config, BUCKET_NAME, OBJECT_NAME, ""));
    }
}
