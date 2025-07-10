package eu.isygoit.storage;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.api.impl.MinIOApiService;
import eu.isygoit.storage.exception.MinIoObjectException;
import eu.isygoit.storage.object.FileStorage;
import eu.isygoit.storage.object.StorageConfig;
import eu.isygoit.storage.service.MinIOService;
import io.minio.BucketExistsArgs;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class StorageApplicationTest {

    @Container
    private static final MinIOContainer minioContainer = new MinIOContainer("minio/minio:latest")
            .withUserName("testuser")
            .withPassword("testpassword");

    @Autowired
    private MinIOService minIOService;

    private StorageConfig storageConfig;
    private static final String BUCKET_NAME = "test-bucket";
    private static final String OBJECT_NAME = "test-object.txt";
    private static final String TENANT = "test-tenant";

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", minioContainer::getS3URL);
        registry.add("minio.username", minioContainer::getUserName);
        registry.add("minio.password", minioContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        storageConfig = new StorageConfig();
        storageConfig.setTenant(TENANT);
        storageConfig.setUrl(minioContainer.getS3URL());
        storageConfig.setUserName(minioContainer.getUserName());
        storageConfig.setPassword(minioContainer.getPassword());
    }

    @Test
    @Order(1)
    void testCreateAndCheckBucket() {
        minIOService.makeBucket(storageConfig, BUCKET_NAME);
        boolean exists = minIOService.bucketExists(storageConfig, BUCKET_NAME);
        assertTrue(exists, "Bucket should exist after creation");
    }

    @Test
    @Order(2)
    void testSetVersioning() {
        minIOService.makeBucket(storageConfig, BUCKET_NAME);

        assertTrue(minIOService.bucketExists(storageConfig, BUCKET_NAME), "Bucket should exist");

        minIOService.setVersioningBucket(storageConfig, BUCKET_NAME, true);
        MinioClient client = minIOService.getConnection(storageConfig);
        try {
            VersioningConfiguration config = client.getBucketVersioning(
                    GetBucketVersioningArgs.builder().build().builder().bucket(BUCKET_NAME).build());
            assertEquals(VersioningConfiguration.Status.ENABLED, config.status(),
                    "Bucket versioning should be enabled");
        } catch (Exception e) {
            fail("Failed to check versioning status: " + e.getMessage());
        }

        minIOService.setVersioningBucket(storageConfig, BUCKET_NAME, false);
        try {
            VersioningConfiguration config = client.getBucketVersioning(
                    GetBucketVersioningArgs.builder().bucket(BUCKET_NAME).build());
            assertEquals(VersioningConfiguration.Status.SUSPENDED, config.status(),
                    "Bucket versioning should be suspended");
        } catch (Exception e) {
            fail("Failed to check versioning status: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void testUploadAndGetObject() {
        String content = "Test content";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                OBJECT_NAME,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );

        Map<String, String> tags = new HashMap<>();
        tags.put("type", "document");
        tags.put("category", "test");

        minIOService.uploadFile(storageConfig, BUCKET_NAME, "", OBJECT_NAME, file, tags);

        byte[] retrievedContent = minIOService.getObject(storageConfig, BUCKET_NAME, OBJECT_NAME, null);
        assertEquals(content, new String(retrievedContent, StandardCharsets.UTF_8),
                "Retrieved content should match uploaded content");
    }

    @Test
    @Order(4)
    void testGetPresignedUrl() {
        String content = "Test content for presigned URL";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                OBJECT_NAME,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );

        minIOService.uploadFile(storageConfig, BUCKET_NAME, "", OBJECT_NAME, file, null);
        String url = minIOService.getPresignedObjectUrl(storageConfig, BUCKET_NAME, OBJECT_NAME);
        assertNotNull(url, "Presigned URL should not be null");
        assertTrue(url.contains(OBJECT_NAME), "Presigned URL should contain object name");
    }

    @Test
    @Order(5)
    void testGetObjectsByTags() {
        String content = "Tagged content";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                OBJECT_NAME,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );

        Map<String, String> tags = new HashMap<>();
        tags.put("type", "document");
        tags.put("env", "test");

        minIOService.uploadFile(storageConfig, BUCKET_NAME, "", OBJECT_NAME, file, tags);

        Map<String, String> searchTags = new HashMap<>();
        searchTags.put("type", "document");

        List<FileStorage> objects = minIOService.getObjectByTags(
                storageConfig, BUCKET_NAME, searchTags, IEnumLogicalOperator.Types.AND);

        assertFalse(objects.isEmpty(), "Should find objects with matching tags");
        assertTrue(objects.stream().anyMatch(obj -> obj.getObjectName().equals(OBJECT_NAME)),
                "Found objects should include the uploaded object");
    }

    @Test
    @Order(6)
    void testUpdateTags() {
        String content = "Content for tag update";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                OBJECT_NAME,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );

        Map<String, String> initialTags = new HashMap<>();
        initialTags.put("initial", "value");
        minIOService.uploadFile(storageConfig, BUCKET_NAME, "", OBJECT_NAME, file, initialTags);

        Map<String, String> newTags = new HashMap<>();
        newTags.put("updated", "new-value");
        minIOService.updateTags(storageConfig, BUCKET_NAME, OBJECT_NAME, newTags);

        List<FileStorage> objects = minIOService.getObjectByTags(
                storageConfig, BUCKET_NAME, newTags, IEnumLogicalOperator.Types.AND);

        assertFalse(objects.isEmpty(), "Should find objects with updated tags");
        assertTrue(objects.stream().anyMatch(obj -> obj.getObjectName().equals(OBJECT_NAME)),
                "Found objects should include the object with updated tags");
    }

    @Test
    @Order(7)
    void testDeleteObject() {
        String content = "Content to delete";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                OBJECT_NAME,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );

        minIOService.uploadFile(storageConfig, BUCKET_NAME, "", OBJECT_NAME, file, null);
        minIOService.deleteObject(storageConfig, BUCKET_NAME, OBJECT_NAME);

        assertThrows(MinIoObjectException.class,
                () -> minIOService.getObject(storageConfig, BUCKET_NAME, OBJECT_NAME, null),
                "Should throw exception when retrieving deleted object");
    }

    @Test
    @Order(8)
    void testDeleteMultipleObjects() {
        String object1 = "multi-test1.txt";
        String object2 = "multi-test2.txt";

        MockMultipartFile file1 = new MockMultipartFile(
                "file1",
                object1,
                "text/plain",
                "Content 1".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file2",
                object2,
                "text/plain",
                "Content 2".getBytes(StandardCharsets.UTF_8)
        );

        minIOService.uploadFile(storageConfig, BUCKET_NAME, "", object1, file1, null);
        minIOService.uploadFile(storageConfig, BUCKET_NAME, "", object2, file2, null);

        List<DeleteObject> objectsToDelete = Arrays.asList(
                new DeleteObject(object1),
                new DeleteObject(object2)
        );

        minIOService.deleteObjects(storageConfig, BUCKET_NAME, objectsToDelete);

        assertThrows(MinIoObjectException.class,
                () -> minIOService.getObject(storageConfig, BUCKET_NAME, object1, null),
                "Should throw exception for deleted object1");
        assertThrows(MinIoObjectException.class,
                () -> minIOService.getObject(storageConfig, BUCKET_NAME, object2, null),
                "Should throw exception for deleted object2");
    }

    @Test
    @Order(9)
    void testListBuckets() {
        minIOService.makeBucket(storageConfig, BUCKET_NAME);
        List<Bucket> buckets = minIOService.getBuckets(storageConfig);

        assertFalse(buckets.isEmpty(), "Should list at least one bucket");
        assertTrue(buckets.stream().anyMatch(bucket -> bucket.name().equals(BUCKET_NAME)),
                "Listed buckets should include the created bucket");
    }

    @Test
    @Order(10)
    void testDeleteBucket() {
        minIOService.makeBucket(storageConfig, BUCKET_NAME);
        minIOService.deleteBucket(storageConfig, BUCKET_NAME);

        boolean exists = minIOService.bucketExists(storageConfig, BUCKET_NAME);
        assertFalse(exists, "Bucket should not exist after deletion");
    }

    @Test
    @Order(11)
    void testInvalidConfig() {
        StorageConfig invalidConfig = new StorageConfig();
        invalidConfig.setTenant("");
        invalidConfig.setUrl("");
        invalidConfig.setUserName("");
        invalidConfig.setPassword("");

        assertThrows(IllegalArgumentException.class,
                () -> minIOService.getConnection(invalidConfig),
                "Should throw exception for invalid configuration");
    }

    @Test
    @Order(12)
    void testInvalidBucketName() {
        assertThrows(IllegalArgumentException.class,
                () -> minIOService.bucketExists(storageConfig, ""),
                "Should throw exception for empty bucket name");
    }

    @Test
    @Order(13)
    void testInvalidObjectParams() {
        assertThrows(IllegalArgumentException.class,
                () -> minIOService.getObject(storageConfig, BUCKET_NAME, "", null),
                "Should throw exception for empty object name");
    }

    @Test
    @Order(14)
    void testInvalidUploadParams() {
        assertThrows(IllegalArgumentException.class,
                () -> minIOService.uploadFile(storageConfig, BUCKET_NAME, "", OBJECT_NAME, null, null),
                "Should throw exception for null multipart file");
    }
}