package eu.isygoit.storage.s3;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.lfs.service.LakeFSService;
import eu.isygoit.storage.s3.object.FileStorage;
import eu.isygoit.storage.s3.object.StorageConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

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

    @Container
    private static GenericContainer<?> lakeFSContainer = new GenericContainer<>("treeverse/lakefs:latest")
            .withExposedPorts(8000)
            .withEnv("LAKEFS_AUTH_ACCESS_KEY_ID", "AKIAIOSFODNN7EXAMPLE")
            .withEnv("LAKEFS_AUTH_SECRET_ACCESS_KEY", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
            .withEnv("LAKEFS_AUTH_ENCRYPT_SECRET_KEY", "my-secure-secret-key-1234567890abcdef")
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
        } catch (Exception e) {
            System.err.println("Failed to start LakeFS container: " + e.getMessage());
            System.err.println("Container logs: " + lakeFSContainer.getLogs());
            throw new RuntimeException("Container startup failed", e);
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
        config.setUserName("AKIAIOSFODNN7EXAMPLE");
        config.setPassword("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

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
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String storageNamespace = "local://test-bucket";
        String defaultBranch = "main";

        assertDoesNotThrow(() -> lakeFSService.createRepository(config, repositoryName, storageNamespace, defaultBranch));
        boolean exists = lakeFSService.repositoryExists(config, repositoryName);
        assertTrue(exists);
    }

    @Test
    @Order(4)
    public void testRepositoryExists() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", "main");
        boolean exists = lakeFSService.repositoryExists(config, repositoryName);
        assertTrue(exists);
    }

    @Test
    @Order(5)
    public void testBranchExists() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);

        boolean exists = lakeFSService.branchExists(config, repositoryName, branchName);
        assertTrue(exists);
    }

    @Test
    @Order(6)
    public void testCreateBranch() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String branchName = "feature";
        String sourceBranch = "main";
        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", sourceBranch);

        assertDoesNotThrow(() -> lakeFSService.createBranch(config, repositoryName, branchName, sourceBranch));
        boolean exists = lakeFSService.branchExists(config, repositoryName, branchName);
        assertTrue(exists);
    }

    @Test
    @Order(7)
    public void testUploadFile() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        String path = "data";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
        Map<String, String> metadata = Map.of("key", "value");

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);

        assertDoesNotThrow(() -> lakeFSService.uploadFile(config, repositoryName, branchName, path, objectName, multipartFile, metadata));
    }

    @Test
    @Order(8)
    public void testGetObject() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);
        lakeFSService.uploadFile(config, repositoryName, branchName, "", objectName, multipartFile, null);

        byte[] result = lakeFSService.getObject(config, repositoryName, branchName, objectName);
        assertArrayEquals("Hello".getBytes(), result);
    }

    @Test
    @Order(9)
    public void testGetPresignedObjectUrl() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);
        lakeFSService.uploadFile(config, repositoryName, branchName, "", objectName, multipartFile, null);

        String url = lakeFSService.getPresignedObjectUrl(config, repositoryName, branchName, objectName);
        assertNotNull(url);
        assertTrue(url.startsWith("http"));
    }

    @Test
    @Order(10)
    public void testDeleteObject() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);
        lakeFSService.uploadFile(config, repositoryName, branchName, "", objectName, multipartFile, null);

        assertDoesNotThrow(() -> lakeFSService.deleteObject(config, repositoryName, branchName, objectName));
    }

    @Test
    @Order(11)
    public void testGetObjectByMetadata() {
        String repositoryName = "test-validated-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
        Map<String, String> metadata = Map.of("key", "value");

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);
        lakeFSService.uploadFile(config, repositoryName, branchName, "", objectName, multipartFile, metadata);

        List<FileStorage> objects = lakeFSService.getObjectByMetadata(config, repositoryName, branchName, metadata, IEnumLogicalOperator.Types.AND);
        assertFalse(objects.isEmpty());
        assertEquals(objectName, objects.get(0).objectName);
    }

    @Test
    @Order(12)
    public void testGetObjects() {
        String repositoryName = "test-validated-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);
        lakeFSService.uploadFile(config, repositoryName, branchName, "", objectName, multipartFile, null);

        List<FileStorage> objects = lakeFSService.getObjects(config, repositoryName, branchName, null);
        assertFalse(objects.isEmpty());
        assertEquals(objectName, objects.get(0).objectName);
    }

    @Test
    @Order(13)
    public void testUpdateMetadata() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
        Map<String, String> metadata = Map.of("key", "new-value");

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);
        lakeFSService.uploadFile(config, repositoryName, branchName, "", objectName, multipartFile, null);

        assertDoesNotThrow(() -> lakeFSService.updateMetadata(config, repositoryName, branchName, objectName, metadata));
    }

    @Test
    @Order(14)
    public void testDeleteObjects() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        List<String> objectNames = List.of("test1.txt", "test2.txt");
        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "test1.txt", "text/plain", "Hello1".getBytes());
        MockMultipartFile multipartFile2 = new MockMultipartFile("file", "test2.txt", "text/plain", "Hello2".getBytes());

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);
        lakeFSService.uploadFile(config, repositoryName, branchName, "", "test1.txt", multipartFile1, null);
        lakeFSService.uploadFile(config, repositoryName, branchName, "", "test2.txt", multipartFile2, null);

        assertDoesNotThrow(() -> lakeFSService.deleteObjects(config, repositoryName, branchName, objectNames));
    }

    @Test
    @Order(15)
    public void testCommit() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        String message = "Test commit";
        Map<String, String> metadata = Map.of("key", "value");

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);

        String commitId = lakeFSService.commit(config, repositoryName, branchName, message, metadata);
        assertNotNull(commitId);
    }

    @Test
    @Order(16)
    public void testMerge() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String sourceBranch = "feature";
        String destBranch = "main";
        String message = "Merge feature into main";

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", destBranch);
        lakeFSService.createBranch(config, repositoryName, sourceBranch, destBranch);

        String mergeCommitId = lakeFSService.merge(config, repositoryName, sourceBranch, destBranch, message);
        assertNotNull(mergeCommitId);
    }

    @Test
    @Order(17)
    public void testGetBranches() {
        String repositoryName = "test-validated-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);

        List<String> branches = lakeFSService.getBranches(config, repositoryName);
        assertEquals(1, branches.size());
        assertTrue(branches.contains("main"));
    }

    @Test
    @Order(18)
    public void testGetRepositories() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", "main");

        List<String> repositories = lakeFSService.getRepositories(config);
        assertTrue(repositories.contains(repositoryName));
    }

    @Test
    @Order(19)
    public void testGetCommitHistory() {
        String repositoryName = "test-validated-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        int limit = 2;
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);
        lakeFSService.uploadFile(config, repositoryName, branchName, "", "test.txt", multipartFile, null);
        lakeFSService.commit(config, repositoryName, branchName, "Initial commit", null);

        List<Map<String, Object>> commits = lakeFSService.getCommitHistory(config, repositoryName, branchName, limit);
        assertFalse(commits.isEmpty());
    }

    @Test
    @Order(20)
    public void testGetDiff() {
        String repositoryName = "test-validated-repo-" + UUID.randomUUID().toString();
        String leftRef = "main";
        String rightRef = "feature";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", leftRef);
        lakeFSService.createBranch(config, repositoryName, rightRef, leftRef);
        lakeFSService.uploadFile(config, repositoryName, rightRef, "", "test.txt", multipartFile, null);

        List<Map<String, Object>> diffs = lakeFSService.getDiff(config, repositoryName, leftRef, rightRef);
        assertFalse(diffs.isEmpty());
    }

    @Test
    @Order(21)
    public void testRevert() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String branchName = "main";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", branchName);
        lakeFSService.uploadFile(config, repositoryName, branchName, "", "test.txt", multipartFile, null);
        String commitId = lakeFSService.commit(config, repositoryName, branchName, "Initial commit", null);

        assertDoesNotThrow(() -> lakeFSService.revert(config, repositoryName, branchName, commitId));
    }

    @Test
    @Order(22)
    public void testDeleteBranch() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        String branchName = "feature";
        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", "main");
        lakeFSService.createBranch(config, repositoryName, branchName, "main");

        assertDoesNotThrow(() -> lakeFSService.deleteBranch(config, repositoryName, branchName));
        assertFalse(lakeFSService.branchExists(config, repositoryName, branchName));
    }

    @Test
    @Order(23)
    public void testDeleteRepository() {
        String repositoryName = "test-repo-" + UUID.randomUUID().toString();
        lakeFSService.createRepository(config, repositoryName, "local://test-bucket", "main");

        assertDoesNotThrow(() -> lakeFSService.deleteRepository(config, repositoryName));
        assertFalse(lakeFSService.repositoryExists(config, repositoryName));
    }
}