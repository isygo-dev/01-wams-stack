package eu.isygoit.storage.s3;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.exception.OxiCloudObjectException;
import eu.isygoit.storage.s3.object.FileStorage;
import eu.isygoit.storage.s3.config.S3Config;
import eu.isygoit.storage.s3.service.OxiCloudService;
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
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("OxiCloud")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OxiCloudStorageApplicationTest {

    @Container
    private static final MinIOContainer minioContainer = new MinIOContainer("minio/minio:latest")
            .withUserName("testuser")
            .withPassword("testpassword");

    @Autowired
    private OxiCloudService oxiCloudService;

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
        oxiCloudService.makeBucket(s3Config, BUCKET_NAME);
        assertTrue(oxiCloudService.bucketExists(s3Config, BUCKET_NAME));
    }

    @Test @Order(2)
    void testSetVersioning() {
        oxiCloudService.makeBucket(s3Config, BUCKET_NAME);

        oxiCloudService.setVersioningBucket(s3Config, BUCKET_NAME, true);
        software.amazon.awssdk.services.s3.S3Client client = oxiCloudService.getConnection(s3Config);
        try {
            GetBucketVersioningResponse config = client.getBucketVersioning(
                    GetBucketVersioningRequest.builder().bucket(BUCKET_NAME).build());
            assertEquals(BucketVersioningStatus.ENABLED, config.status());
        } catch (Exception e) {
            fail("Failed to check versioning: " + e.getMessage());
        }

        oxiCloudService.setVersioningBucket(s3Config, BUCKET_NAME, false);
        try {
            GetBucketVersioningResponse config = client.getBucketVersioning(
                    GetBucketVersioningRequest.builder().bucket(BUCKET_NAME).build());
            assertEquals(BucketVersioningStatus.SUSPENDED, config.status());
        } catch (Exception e) {
            fail("Failed to check versioning: " + e.getMessage());
        }
    }

    @Test @Order(3)
    void testUploadAndGetObject() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Test content".getBytes(StandardCharsets.UTF_8));
        oxiCloudService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, null);
        byte[] data = oxiCloudService.getObject(s3Config, BUCKET_NAME, OBJECT_NAME, null);
        assertEquals("Test content", new String(data, StandardCharsets.UTF_8));
    }

    @Test @Order(4)
    void testGetPresignedUrl() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Content for presigned".getBytes(StandardCharsets.UTF_8));
        oxiCloudService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, null);
        String url = oxiCloudService.getPresignedObjectUrl(s3Config, BUCKET_NAME, OBJECT_NAME);
        assertNotNull(url);
        assertTrue(url.contains(OBJECT_NAME));
    }

    @Test @Order(5)
    void testGetObjectsByTags() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Tagged content".getBytes(StandardCharsets.UTF_8));
        Map<String, String> tags = Map.of("type", "document", "env", "test");
        oxiCloudService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, tags);

        Map<String, String> searchTags = Map.of("type", "document");
        List<FileStorage> results = oxiCloudService.getObjectByTags(
                s3Config, BUCKET_NAME, searchTags, IEnumLogicalOperator.Types.AND);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(f -> f.getObjectName().equals(OBJECT_NAME)));
    }

    @Test @Order(6)
    void testUpdateTags() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Content to tag".getBytes(StandardCharsets.UTF_8));
        oxiCloudService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, Map.of("old", "value"));

        oxiCloudService.updateTags(s3Config, BUCKET_NAME, OBJECT_NAME, Map.of("updated", "yes"));

        List<FileStorage> results = oxiCloudService.getObjectByTags(
                s3Config, BUCKET_NAME, Map.of("updated", "yes"), IEnumLogicalOperator.Types.AND);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(f -> f.getObjectName().equals(OBJECT_NAME)));
    }

    @Test @Order(7)
    void testDeleteObject() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "Content to delete".getBytes(StandardCharsets.UTF_8));
        oxiCloudService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, null);
        oxiCloudService.deleteObject(s3Config, BUCKET_NAME, OBJECT_NAME);
        assertThrows(OxiCloudObjectException.class, () ->
                oxiCloudService.getObject(s3Config, BUCKET_NAME, OBJECT_NAME, null));
    }

    @Test @Order(8)
    void testDeleteMultipleObjects() {
        MockMultipartFile f1 = new MockMultipartFile("f1", "file1.txt", "text/plain", "1".getBytes());
        MockMultipartFile f2 = new MockMultipartFile("f2", "file2.txt", "text/plain", "2".getBytes());

        oxiCloudService.uploadFile(s3Config, BUCKET_NAME, "", "file1.txt", f1, null);
        oxiCloudService.uploadFile(s3Config, BUCKET_NAME, "", "file2.txt", f2, null);

        oxiCloudService.deleteObjects(s3Config, BUCKET_NAME,
                List.of(DeleteObjectRequest.builder().key("file1.txt").build(),
                        DeleteObjectRequest.builder().key("file2.txt").build()));

        assertThrows(OxiCloudObjectException.class, () ->
                oxiCloudService.getObject(s3Config, BUCKET_NAME, "file1.txt", null));
        assertThrows(OxiCloudObjectException.class, () ->
                oxiCloudService.getObject(s3Config, BUCKET_NAME, "file2.txt", null));
    }

    @Test @Order(9)
    void testListBuckets() {
        List<Bucket> buckets = oxiCloudService.getBuckets(s3Config);
        assertFalse(buckets.isEmpty());
        assertTrue(buckets.stream().anyMatch(b -> b.name().equals(BUCKET_NAME)));
    }

    @Test @Order(10)
    void testDeleteBucket() {
        oxiCloudService.makeBucket(s3Config, "bucket-delete");
        oxiCloudService.deleteBucket(s3Config, "bucket-delete");
        assertFalse(oxiCloudService.bucketExists(s3Config, "bucket-delete"));
    }

    @Test @Order(11)
    void testInvalidConfig() {
        S3Config invalid = new S3Config();
        assertThrows(IllegalArgumentException.class, () -> oxiCloudService.getConnection(invalid));
    }

    @Test @Order(12)
    void testInvalidBucketName() {
        assertThrows(IllegalArgumentException.class, () -> oxiCloudService.bucketExists(s3Config, ""));
    }

    @Test @Order(13)
    void testInvalidObjectName() {
        assertThrows(IllegalArgumentException.class,
                () -> oxiCloudService.getObject(s3Config, BUCKET_NAME, "", null));
    }

    @Test @Order(14)
    void testInvalidUploadParams() {
        assertThrows(IllegalArgumentException.class,
                () -> oxiCloudService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, null, null));
    }

    @Test @Order(15)
    void testUpdateConnection() {
        assertDoesNotThrow(() -> oxiCloudService.updateConnection(s3Config));
    }

    @Test @Order(16)
    void testUploadWithPath() {
        String objectPath = "/dir1/dir2/";
        String objectName = "path-object.txt";
        MockMultipartFile file = new MockMultipartFile("file", objectPath, "text/plain",
                "Path content".getBytes(StandardCharsets.UTF_8));
        oxiCloudService.uploadFile(s3Config, BUCKET_NAME, objectPath, objectName, file, null);
        byte[] retrieved = oxiCloudService.getObject(s3Config, BUCKET_NAME, objectPath + objectName, null);
        assertEquals("Path content", new String(retrieved));
    }

    @Test @Order(17)
    void testListObjectsInBucket() {
        MockMultipartFile file = new MockMultipartFile("file", "list.txt", "text/plain", "list".getBytes());
        oxiCloudService.uploadFile(s3Config, BUCKET_NAME, "", "list.txt", file, null);
        List<FileStorage> files = oxiCloudService.getObjects(s3Config, BUCKET_NAME);
        assertTrue(files.stream().anyMatch(f -> f.getObjectName().equals("list.txt")));
    }

    @Test @Order(18)
    void testGetObjectWithEmptyVersionId() {
        MockMultipartFile file = new MockMultipartFile("file", OBJECT_NAME, "text/plain",
                "With empty version".getBytes());
        oxiCloudService.uploadFile(s3Config, BUCKET_NAME, "", OBJECT_NAME, file, null);
        assertDoesNotThrow(() ->
                oxiCloudService.getObject(s3Config, BUCKET_NAME, OBJECT_NAME, ""));
    }
}
