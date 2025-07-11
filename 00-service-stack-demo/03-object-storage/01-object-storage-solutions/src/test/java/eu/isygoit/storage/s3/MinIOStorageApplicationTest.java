package eu.isygoit.storage.s3;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.exception.MinIoObjectException;
import eu.isygoit.storage.s3.object.FileStorage;
import eu.isygoit.storage.s3.config.S3Config;
import eu.isygoit.storage.s3.service.MinIOService;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MinIOStorageApplicationTest {

    @Container
    private static final MinIOContainer minioContainer = new MinIOContainer("minio/minio:latest")
            .withUserName("testuser")
            .withPassword("testpassword");

    @Autowired
    private MinIOService minIOService;

    private S3Config s3Config;
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
        s3Config = new S3Config();
        s3Config.setTenant(TENANT);
        s3Config.setUrl(minioContainer.getS3URL());
        s3Config.setUserName(minioContainer.getUserName());
        s3Config.setPassword(minioContainer.getPassword());
    }

    @Test @Order(1)
    void testCreateAndCheckBucket() {
        minIOService.makeBucket(s3Config, BUCKET_NAME);
        assertTrue(minIOService.bucketExists(s3Config, BUCKET_NAME));
    }

    @Test @Order(2)
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

    @Test @Order(3)
    void testUploadAndGetObject() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Test content".getBytes(StandardCharsets.UTF_8));
        minIOService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, null);
        byte[] data = minIOService.getObject(s3Config, BUCKET_NAME, OBJECT_NAME, null);
        assertEquals("Test content", new String(data, StandardCharsets.UTF_8));
    }

    @Test @Order(4)
    void testGetPresignedUrl() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Content for presigned".getBytes(StandardCharsets.UTF_8));
        minIOService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, null);
        String url = minIOService.getPresignedObjectUrl(s3Config, BUCKET_NAME, OBJECT_NAME);
        assertNotNull(url);
        assertTrue(url.contains(OBJECT_NAME));
    }

    @Test @Order(5)
    void testGetObjectsByTags() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Tagged content".getBytes(StandardCharsets.UTF_8));
        Map<String, String> tags = Map.of("type", "document", "env", "test");
        minIOService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, tags);

        Map<String, String> searchTags = Map.of("type", "document");
        List<FileStorage> results = minIOService.getObjectByTags(
                s3Config, BUCKET_NAME, searchTags, IEnumLogicalOperator.Types.AND);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(f -> f.getObjectName().equals(OBJECT_NAME)));
    }

    @Test @Order(6)
    void testUpdateTags() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Content to tag".getBytes(StandardCharsets.UTF_8));
        minIOService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, Map.of("old", "value"));

        minIOService.updateTags(s3Config, BUCKET_NAME, OBJECT_NAME, Map.of("updated", "yes"));

        List<FileStorage> results = minIOService.getObjectByTags(
                s3Config, BUCKET_NAME, Map.of("updated", "yes"), IEnumLogicalOperator.Types.AND);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(f -> f.getObjectName().equals(OBJECT_NAME)));
    }

    @Test @Order(7)
    void testDeleteObject() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Content to delete".getBytes(StandardCharsets.UTF_8));
        minIOService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, null);
        minIOService.deleteObject(s3Config, BUCKET_NAME, OBJECT_NAME);
        assertThrows(MinIoObjectException.class, () ->
                minIOService.getObject(s3Config, BUCKET_NAME, OBJECT_NAME, null));
    }

    @Test @Order(8)
    void testDeleteMultipleObjects() {
        MockMultipartFile f1 = new MockMultipartFile("f1", "file1.txt", "text/plain", "1".getBytes());
        MockMultipartFile f2 = new MockMultipartFile("f2", "file2.txt", "text/plain", "2".getBytes());

        minIOService.uploadFile(s3Config, BUCKET_NAME, "", "file1.txt", f1, null);
        minIOService.uploadFile(s3Config, BUCKET_NAME, "", "file2.txt", f2, null);

        minIOService.deleteObjects(s3Config, BUCKET_NAME,
                List.of(new DeleteObject("file1.txt"), new DeleteObject("file2.txt")));

        assertThrows(MinIoObjectException.class, () ->
                minIOService.getObject(s3Config, BUCKET_NAME, "file1.txt", null));
        assertThrows(MinIoObjectException.class, () ->
                minIOService.getObject(s3Config, BUCKET_NAME, "file2.txt", null));
    }

    @Test @Order(9)
    void testListBuckets() {
        List<Bucket> buckets = minIOService.getBuckets(s3Config);
        assertFalse(buckets.isEmpty());
        assertTrue(buckets.stream().anyMatch(b -> b.name().equals(BUCKET_NAME)));
    }

    @Test @Order(10)
    void testDeleteBucket() {
        minIOService.makeBucket(s3Config, "bucket-delete");
        minIOService.deleteBucket(s3Config, "bucket-delete");
        assertFalse(minIOService.bucketExists(s3Config, "bucket-delete"));
    }

    @Test @Order(11)
    void testInvalidConfig() {
        S3Config invalid = new S3Config();
        assertThrows(IllegalArgumentException.class, () -> minIOService.getConnection(invalid));
    }

    @Test @Order(12)
    void testInvalidBucketName() {
        assertThrows(IllegalArgumentException.class, () -> minIOService.bucketExists(s3Config, ""));
    }

    @Test @Order(13)
    void testInvalidObjectName() {
        assertThrows(IllegalArgumentException.class,
                () -> minIOService.getObject(s3Config, BUCKET_NAME, "", null));
    }

    @Test @Order(14)
    void testInvalidUploadParams() {
        assertThrows(IllegalArgumentException.class,
                () -> minIOService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, null, null));
    }

    @Test @Order(15)
    void testUpdateConnection() {
        assertDoesNotThrow(() -> minIOService.updateConnection(s3Config));
    }

    @Test @Order(16)
    void testUploadWithPath() {
        String objectPath = "/dir1/dir2/";
        String objectName = "path-object.txt";
        MockMultipartFile file = new MockMultipartFile("file", objectPath, "text/plain",
                "Path content".getBytes(StandardCharsets.UTF_8));
        minIOService.uploadFile(s3Config, BUCKET_NAME, objectPath, objectName, file, null);
        byte[] retrieved = minIOService.getObject(s3Config, BUCKET_NAME, objectPath + objectName, null);
        assertEquals("Path content", new String(retrieved));
    }

    @Test @Order(17)
    void testListObjectsInBucket() {
        MockMultipartFile file = new MockMultipartFile("file", "list.txt", "text/plain", "list".getBytes());
        minIOService.uploadFile(s3Config, BUCKET_NAME, "", "list.txt", file, null);
        List<FileStorage> files = minIOService.getObjects(s3Config, BUCKET_NAME);
        assertTrue(files.stream().anyMatch(f -> f.getObjectName().equals("list.txt")));
    }

    @Test @Order(18)
    void testGetObjectWithEmptyVersionId() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "With empty version".getBytes());
        minIOService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, null);
        assertDoesNotThrow(() ->
                minIOService.getObject(s3Config, BUCKET_NAME, OBJECT_NAME, ""));
    }
}
