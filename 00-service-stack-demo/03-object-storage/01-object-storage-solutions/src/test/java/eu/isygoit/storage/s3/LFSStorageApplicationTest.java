package eu.isygoit.storage.s3;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.exception.LakeFSObjectException;
import eu.isygoit.storage.lfs.config.LFSConfig;
import eu.isygoit.storage.lfs.service.LakeFSService;
import eu.isygoit.storage.s3.config.S3Config;
import eu.isygoit.storage.s3.object.FileStorage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles(profiles = {
        "LakeFS",
        "MinIO",
})
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LFSStorageApplicationTest {

    @Autowired
    private LakeFSService lakeFSService;

    @Autowired
    private Map<String, RestTemplate> lakeFSClientMap;

    private static final String ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";
    private static final String SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
    private static final String ENCRYPT_SECRET_KEY = "my-secure-secret-key-1234567890abcdef";
    private static final String MINIO_ACCESS_KEY = "minioadmin";
    private static final String MINIO_SECRET_KEY = "minioadmin";

    private static Network network = Network.newNetwork();

    @Container
    private static GenericContainer<?> minioContainer = new GenericContainer<>("minio/minio:latest")
            .withNetwork(network)
            .withNetworkAliases("minio")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", MINIO_ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", MINIO_SECRET_KEY)
            .withCommand("server /data --console-address :9001")
            .waitingFor(Wait.forHttp("/minio/health/live")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(5)));

    @Container
    private static GenericContainer<?> lakeFSContainer = new GenericContainer<>("treeverse/lakefs:latest")
            .withNetwork(network)
            .withNetworkAliases("lakefs")
            .withExposedPorts(8000)
            .withEnv("LAKEFS_AUTH_ACCESS_KEY_ID", ACCESS_KEY)
            .withEnv("LAKEFS_AUTH_SECRET_ACCESS_KEY", SECRET_KEY)
            .withEnv("LAKEFS_AUTH_ENCRYPT_SECRET_KEY", ENCRYPT_SECRET_KEY)
            .withEnv("LAKEFS_BLOCKSTORE_TYPE", "s3")
            .withEnv("LAKEFS_BLOCKSTORE_S3_ACCESS_KEY_ID", MINIO_ACCESS_KEY)
            .withEnv("LAKEFS_BLOCKSTORE_S3_SECRET_ACCESS_KEY", MINIO_SECRET_KEY)
            .withEnv("LAKEFS_BLOCKSTORE_S3_ENDPOINT", "http://minio:9000")
            .withEnv("LAKEFS_BLOCKSTORE_S3_REGION", "us-east-1")
            .withEnv("LAKEFS_BLOCKSTORE_S3_FORCE_PATH_STYLE", "true")
            .withEnv("AWS_EC2_METADATA_DISABLED", "true")
            .withEnv("AWS_ACCESS_KEY_ID", MINIO_ACCESS_KEY)
            .withEnv("AWS_SECRET_ACCESS_KEY", MINIO_SECRET_KEY)
            .withEnv("AWS_REGION", "us-east-1")
            .withEnv("LAKEFS_DATABASE_TYPE", "mem")
            .withEnv("LAKEFS_STATS_ENABLED", "false")
            .withEnv("LAKEFS_LOGGING_LEVEL", "DEBUG")
            .withEnv("LAKEFS_GATEWAYS_S3_DOMAIN_NAME", "s3.local.lakefs.io")
            .withCommand("run")
            .waitingFor(Wait.forHttp("/api/v1/setup_lakefs")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(10)))
            .dependsOn(minioContainer);

    private LFSConfig config;
    private String tenant;

    @BeforeAll
    public static void setUpContainer() {
        try {
            minioContainer.start();
            System.out.println("MinIO container started successfully");
            System.out.println("MinIO Container ID: " + minioContainer.getContainerId());
            System.out.println("MinIO Container logs: " + minioContainer.getLogs());
            System.out.println("MinIO Container status: " + minioContainer.getContainerInfo().getState().getStatus());

            lakeFSContainer.start();
            System.out.println("LakeFS container started successfully");
            System.out.println("Container ID: " + lakeFSContainer.getContainerId());
            System.out.println("Container logs: " + lakeFSContainer.getLogs());
            System.out.println("Container status: " + lakeFSContainer.getContainerInfo().getState().getStatus());

            // Wait for LakeFS to be fully ready
            Thread.sleep(60000);

            // Initialize LakeFS setup
            initializeLakeFS();

        } catch (Exception e) {
            System.err.println("Failed to start containers: " + e.getMessage());
            System.err.println("MinIO Container logs: " + minioContainer.getLogs());
            System.err.println("LakeFS Container logs: " + lakeFSContainer.getLogs());
            throw new RuntimeException("Container startup failed", e);
        }
    }

    private static void initializeLakeFS() {
        try {
            String lakeFSUrl = "http://" + lakeFSContainer.getHost() + ":" + lakeFSContainer.getMappedPort(8000);
            System.out.println("Initializing LakeFS at URL: " + lakeFSUrl);
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> setupRequest = Map.of(
                    "username", "admin",
                    "key", Map.of(
                            "access_key_id", ACCESS_KEY,
                            "secret_access_key", SECRET_KEY
                    )
            );
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(setupRequest, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    lakeFSUrl + "/api/v1/setup_lakefs",
                    entity,
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("LakeFS setup completed successfully, response: " + response.getBody());
            } else {
                System.err.println("LakeFS setup failed with status: " + response.getStatusCode() + ", body: " + response.getBody());
                throw new RuntimeException("LakeFS initialization failed: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize LakeFS: " + e.getMessage());
            System.err.println("LakeFS Container logs: " + lakeFSContainer.getLogs());
            throw new RuntimeException("LakeFS initialization failed", e);
        }
    }

    @BeforeEach
    public void setUp() throws InterruptedException {
        if (!minioContainer.isRunning() || !lakeFSContainer.isRunning()) {
            throw new RuntimeException("Containers are not running. MinIO logs: " + minioContainer.getLogs() + "\nLakeFS logs: " + lakeFSContainer.getLogs());
        }

        System.out.println("MinIO Container host: " + minioContainer.getHost());
        System.out.println("MinIO Container port: " + minioContainer.getMappedPort(9000));
        System.out.println("LakeFS Container host: " + lakeFSContainer.getHost());
        System.out.println("LakeFS Container port: " + lakeFSContainer.getMappedPort(8000));

        tenant = "test-tenant-" + UUID.randomUUID().toString();
        config = new LFSConfig();
        config.setTenant(tenant);
        config.setUrl("http://" + lakeFSContainer.getHost() + ":" + lakeFSContainer.getMappedPort(8000) + "/api/v1");
        config.setUserName(ACCESS_KEY); // Use access_key_id
        config.setPassword(SECRET_KEY);
        config.setS3Config(S3Config.builder()
                .tenant(tenant)
                .url("http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000))
                .userName(MINIO_ACCESS_KEY)
                .password(MINIO_SECRET_KEY)
                .region("us-east-1")
                .build());

        // Verify connection to LakeFS
        int maxRetries = 10;
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                Thread.sleep(2000);
                lakeFSService.getConnection(config);
                System.out.println("Successfully connected to LakeFS on attempt " + (retryCount + 1));
                return;
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                System.out.println("Connection attempt " + retryCount + " failed: " + e.getMessage());
                if (retryCount % 3 == 0) {
                    System.err.println("MinIO Container logs at attempt " + retryCount + ": " + minioContainer.getLogs());
                    System.err.println("LakeFS Container logs at attempt " + retryCount + ": " + lakeFSContainer.getLogs());
                }
            }
        }

        System.err.println("Failed to connect to LakeFS after " + maxRetries + " attempts");
        System.err.println("MinIO Container logs: " + minioContainer.getLogs());
        System.err.println("LakeFS Container logs: " + lakeFSContainer.getLogs());
        throw new RuntimeException("Could not connect to LakeFS", lastException);
    }

    @AfterEach
    public void tearDown() {
        try {
            List<String> repositories = lakeFSService.getRepositories(config);
            for (String repo : repositories) {
                try {
                    lakeFSService.deleteRepository(config, repo);
                } catch (Exception e) {
                    System.err.println("Failed to delete repository " + repo + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to clean up repositories: " + e.getMessage());
        }
        lakeFSClientMap.remove(tenant);
    }

    @Test
    @Order(1)
    public void testGetConnection() {
        RestTemplate result = lakeFSService.getConnection(config);
        assertNotNull(result);
        assertSame(lakeFSClientMap.get(tenant), result);
    }

    @Test
    @Order(2)
    public void testUpdateConnection() {
        lakeFSService.updateConnection(config);
        assertNotNull(lakeFSClientMap.get(tenant));
    }

    @Test
    @Order(3)
    public void testCreateRepository() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";

        assertDoesNotThrow(() -> lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch));
        boolean exists = lakeFSService.repositoryExists(config, repositoryName);
        assertTrue(exists);
    }

    @Test
    @Order(4)
    public void testRepositoryExists() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        boolean exists = lakeFSService.repositoryExists(config, repositoryName);
        assertTrue(exists);
    }

    @Test
    @Order(5)
    public void testBranchExists() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "dev";
        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);

        boolean exists = lakeFSService.branchExists(config, repositoryName, defaultBranch);
        assertTrue(exists);
    }

    @Test
    @Order(6)
    public void testCreateBranch() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String branchName = "feature";
        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);

        assertDoesNotThrow(() -> lakeFSService.createBranch(config, repositoryName, branchName, defaultBranch));
        boolean exists = lakeFSService.branchExists(config, repositoryName, branchName);
        assertTrue(exists);
    }

    @Test
    @Order(7)
    public void testUploadFile() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String path = "data";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
        Map<String, String> metadata = Map.of("key", "value");

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);

        assertDoesNotThrow(() -> lakeFSService.uploadFile(config, repositoryName, defaultBranch, path, objectName, multipartFile, metadata));
    }

    @Test
    @Order(8)
    public void testGetObject() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", objectName, multipartFile, null);

        byte[] result = lakeFSService.getObject(config, repositoryName, defaultBranch, objectName);
        assertArrayEquals("Hello".getBytes(), result);
    }

 /*   @Test
    @Order(9)
    public void testGetPresignedObjectUrl() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.createBranch(config, repositoryName, defaultBranch, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", objectName, multipartFile, null);

        String url = lakeFSService.getPresignedObjectUrl(config, repositoryName, defaultBranch, objectName);
        assertNotNull(url);
        assertTrue(url.startsWith("http"));
    }*/

    @Test
    @Order(10)
    public void testDeleteObject() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", objectName, multipartFile, null);

        assertDoesNotThrow(() -> lakeFSService.deleteObject(config, repositoryName, defaultBranch, objectName));
    }

    @Test
    @Order(11)
    public void testGetObjectByMetadata() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
        Map<String, String> metadata = Map.of("key", "value");

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", objectName, multipartFile, metadata);

        List<FileStorage> objects = lakeFSService.getObjectByMetadata(config, repositoryName, defaultBranch, metadata, IEnumLogicalOperator.Types.AND);
        assertFalse(objects.isEmpty());
        assertEquals(objectName, objects.get(0).objectName);
    }

    @Test
    @Order(12)
    public void testGetObjects() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", objectName, multipartFile, null);

        List<FileStorage> objects = lakeFSService.getObjects(config, repositoryName, defaultBranch, null);
        assertFalse(objects.isEmpty());
        assertEquals(objectName, objects.get(0).objectName);
    }

    @Test
    @Order(13)
    public void testUpdateMetadata() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
        Map<String, String> metadata = Map.of("key", "new-value");

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", objectName, multipartFile, null);

        assertDoesNotThrow(() -> lakeFSService.updateMetadata(config, repositoryName, defaultBranch, objectName, metadata));
    }

    @Test
    @Order(14)
    public void testDeleteObjects() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        List<String> objectNames = List.of("test1.txt", "test2.txt");
        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "test1.txt", "text/plain", "Hello1".getBytes());
        MockMultipartFile multipartFile2 = new MockMultipartFile("file", "test2.txt", "text/plain", "Hello2".getBytes());

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", "test1.txt", multipartFile1, null);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", "test2.txt", multipartFile2, null);

        assertDoesNotThrow(() -> lakeFSService.deleteObjects(config, repositoryName, defaultBranch, objectNames));
    }

    @Test
    @Order(15)
    public void testCommit() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String message = "Test commit";
        Map<String, String> metadata = Map.of("key", "value");
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        // Create repository
        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);

        // Upload a file to create changes in the branch
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", objectName, multipartFile, null);

        // Commit the changes
        String commitId = lakeFSService.commit(config, repositoryName, defaultBranch, message, metadata);
        assertNotNull(commitId);
    }

    @Test
    @Order(15)
    public void testCommitNoChanges() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String message = "Test commit";
        Map<String, String> metadata = Map.of("key", "value");

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);

        assertThrows(LakeFSObjectException.class, () ->
                lakeFSService.commit(config, repositoryName, defaultBranch, message, metadata));
    }

    @Test
    @Order(16)
    public void testMerge() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String featureBranch = "feature";
        String message = "Test merge";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        // Create repository and feature branch
        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.createBranch(config, repositoryName, featureBranch, defaultBranch);

        // Upload a file to the feature branch to create changes
        lakeFSService.uploadFile(config, repositoryName, featureBranch, "", objectName, multipartFile, null);

        // Commit changes in the feature branch
        String commitId = lakeFSService.commit(config, repositoryName, featureBranch, "Add test file", null);
        assertNotNull(commitId);

        // Merge feature branch into main
        String mergeId = lakeFSService.merge(config, repositoryName, featureBranch, defaultBranch, message);
        assertNotNull(mergeId);
    }

    @Test
    @Order(16)
    public void testMergeNoChanges() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String featureBranch = "feature";
        String message = "Test merge";

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.createBranch(config, repositoryName, featureBranch, defaultBranch);

        assertThrows(LakeFSObjectException.class, () ->
                lakeFSService.merge(config, repositoryName, featureBranch, defaultBranch, message));
    }

    @Test
    @Order(17)
    public void testGetBranches() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);

        List<String> branches = lakeFSService.getBranches(config, repositoryName);
        assertEquals(1, branches.size());
        assertTrue(branches.contains("main"));
    }

    @Test
    @Order(18)
    public void testGetRepositories() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);

        List<String> repositories = lakeFSService.getRepositories(config);
        assertTrue(repositories.contains(repositoryName));
    }

    @Test
    @Order(19)
    public void testGetCommitHistory() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        int limit = 2;
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", "test.txt", multipartFile, null);
        lakeFSService.commit(config, repositoryName, defaultBranch, "Initial commit", null);

        List<Map<String, Object>> commits = lakeFSService.getCommitHistory(config, repositoryName, defaultBranch, limit);
        assertFalse(commits.isEmpty());
    }

    @Test
    @Order(20)
    public void testGetDiff() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String rightRef = "feature";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.createBranch(config, repositoryName, rightRef, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, rightRef, "", "test.txt", multipartFile, null);

        List<Map<String, Object>> diffs = lakeFSService.getDiff(config, repositoryName, defaultBranch, rightRef);
        assertFalse(diffs.isEmpty());
    }

    @Test
    @Order(21)
    public void testRevert() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", "test.txt", multipartFile, null);
        String commitId = lakeFSService.commit(config, repositoryName, defaultBranch, "Initial commit", null);

        assertDoesNotThrow(() -> lakeFSService.revert(config, repositoryName, defaultBranch, commitId));
    }

    @Test
    @Order(22)
    public void testDeleteBranch() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        String branchName = "feature";
        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.createBranch(config, repositoryName, branchName, defaultBranch);

        assertDoesNotThrow(() -> lakeFSService.deleteBranch(config, repositoryName, branchName));
        assertFalse(lakeFSService.branchExists(config, repositoryName, branchName));
    }

    @Test
    @Order(23)
    public void testDeleteRepository() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "bucket-" + strUUID;
        String defaultBranch = "main";
        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);

        assertDoesNotThrow(() -> lakeFSService.deleteRepository(config, repositoryName));
        assertFalse(lakeFSService.repositoryExists(config, repositoryName));
    }
}