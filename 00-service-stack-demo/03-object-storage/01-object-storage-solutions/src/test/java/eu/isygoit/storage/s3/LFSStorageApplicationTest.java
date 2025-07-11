package eu.isygoit.storage.s3;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.lfs.service.LakeFSService;
import eu.isygoit.storage.s3.object.FileStorage;
import eu.isygoit.storage.s3.object.StorageConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
    private static final String ENCRYPT_SECRET_KEY = "my-secure-secret-key-1234567890abcdef"; // 32 characters

    @Container
    private static GenericContainer<?> lakeFSContainer = new GenericContainer<>("treeverse/lakefs:latest")
            .withExposedPorts(8000)
            .withEnv("LAKEFS_AUTH_ACCESS_KEY_ID", ACCESS_KEY)
            .withEnv("LAKEFS_AUTH_SECRET_ACCESS_KEY", SECRET_KEY)
            .withEnv("LAKEFS_AUTH_ENCRYPT_SECRET_KEY", ENCRYPT_SECRET_KEY)
            .withEnv("LAKEFS_BLOCKSTORE_TYPE", "local")
            .withEnv("LAKEFS_BLOCKSTORE_LOCAL_PATH", "/lakefs_data")
            .withEnv("LAKEFS_DATABASE_TYPE", "mem")
            .withEnv("LAKEFS_STATS_ENABLED", "false")
            .withEnv("LAKEFS_LOGGING_LEVEL", "DEBUG")
            .withEnv("LAKEFS_GATEWAYS_S3_DOMAIN_NAME", "s3.local.lakefs.io")
            .withFileSystemBind("./lakefs_data", "/lakefs_data", BindMode.READ_WRITE)
            .withCommand("run")
            .waitingFor(Wait.forHttp("/api/v1/setup_lakefs")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(5)))
            .withStartupTimeout(Duration.ofMinutes(5));

    private StorageConfig config;
    private String tenant;

    @BeforeAll
    public static void setUpContainer() {
        try {
            lakeFSContainer.start();
            System.out.println("LakeFS container started successfully");
            System.out.println("Container ID: " + lakeFSContainer.getContainerId());
            System.out.println("Container logs: " + lakeFSContainer.getLogs());
            System.out.println("Container status: " + lakeFSContainer.getContainerInfo().getState().getStatus());

            // Wait for LakeFS to be fully ready
            Thread.sleep(10000);

            // Initialize LakeFS setup
            initializeLakeFS();

        } catch (Exception e) {
            System.err.println("Failed to start LakeFS container: " + e.getMessage());
            System.err.println("Container logs: " + lakeFSContainer.getLogs());
            throw new RuntimeException("Container startup failed", e);
        }
    }

    private static void initializeLakeFS() {
        try {
            String lakeFSUrl = "http://" + lakeFSContainer.getHost() + ":" + lakeFSContainer.getMappedPort(8000);

            // Create RestTemplate for setup
            RestTemplate restTemplate = new RestTemplate();

            // Setup request body for LakeFS initialization
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

            // Call setup endpoint
            ResponseEntity<String> response = restTemplate.postForEntity(
                    lakeFSUrl + "/api/v1/setup_lakefs",
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("LakeFS setup completed successfully");
            } else {
                System.err.println("LakeFS setup failed with status: " + response.getStatusCode());
                System.err.println("Response body: " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("Failed to initialize LakeFS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void setUp() throws InterruptedException {
        if (!lakeFSContainer.isRunning()) {
            throw new RuntimeException("LakeFS container is not running. Logs: " + lakeFSContainer.getLogs());
        }

        System.out.println("Container host: " + lakeFSContainer.getHost());
        System.out.println("Container port: " + lakeFSContainer.getMappedPort(8000));

        tenant = "test-tenant-" + UUID.randomUUID().toString();
        config = new StorageConfig();
        config.setTenant(tenant);
        config.setUrl("http://" + lakeFSContainer.getHost() + ":" + lakeFSContainer.getMappedPort(8000) + "/api/v1");
        config.setUserName(ACCESS_KEY);
        config.setPassword(SECRET_KEY);

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

                // Print container logs for debugging
                if (retryCount % 3 == 0) {
                    System.err.println("Container logs at attempt " + retryCount + ": " + lakeFSContainer.getLogs());
                }
            }
        }

        System.err.println("Failed to connect to LakeFS after " + maxRetries + " attempts");
        System.err.println("Container logs: " + lakeFSContainer.getLogs());
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
        String defaultBranch = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", objectName, multipartFile, null);

        byte[] result = lakeFSService.getObject(config, repositoryName, defaultBranch, objectName);
        assertArrayEquals("Hello".getBytes(), result);
    }

    @Test
    @Order(9)
    public void testGetPresignedObjectUrl() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "local://bucket-" + strUUID;
        String defaultBranch = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.uploadFile(config, repositoryName, defaultBranch, "", objectName, multipartFile, null);

        String url = lakeFSService.getPresignedObjectUrl(config, repositoryName, defaultBranch, objectName);
        assertNotNull(url);
        assertTrue(url.startsWith("http"));
    }

    @Test
    @Order(10)
    public void testDeleteObject() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
        String defaultBranch = "main";
        String message = "Test commit";
        Map<String, String> metadata = Map.of("key", "value");

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);

        String commitId = lakeFSService.commit(config, repositoryName, defaultBranch, message, metadata);
        assertNotNull(commitId);
    }

    @Test
    @Order(16)
    public void testMerge() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "local://bucket-" + strUUID;
        String defaultBranch = "main";
        String sourceBranch = "feature";
        String message = "Merge feature into main";

        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);
        lakeFSService.createBranch(config, repositoryName, sourceBranch, defaultBranch);

        String mergeCommitId = lakeFSService.merge(config, repositoryName, sourceBranch, defaultBranch, message);
        assertNotNull(mergeCommitId);
    }

    @Test
    @Order(17)
    public void testGetBranches() {
        String strUUID = UUID.randomUUID().toString();
        String repositoryName = "test-repo-" + strUUID;
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
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
        String storageNamespace = "local://bucket-" + strUUID;
        String defaultBranch = "main";
        lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch);

        assertDoesNotThrow(() -> lakeFSService.deleteRepository(config, repositoryName));
        assertFalse(lakeFSService.repositoryExists(config, repositoryName));
    }
}