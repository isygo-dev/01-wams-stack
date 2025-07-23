package eu.isygoit.storage.s3;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.exception.LakeFSException;
import eu.isygoit.storage.lfs.config.LFSConfig;
import eu.isygoit.storage.lfs.service.LakeFSService;
import eu.isygoit.storage.s3.config.S3Config;
import eu.isygoit.storage.s3.object.FileStorage;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles(profiles = {"LakeFS", "MinIO"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LFSStorageIntegrationTests {

    private static final String ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";
    private static final String SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
    private static final String ENCRYPT_SECRET_KEY = "my-secure-secret-key-1234567890abcdef";
    private static final String MINIO_ACCESS_KEY = "minioadmin";
    private static final String MINIO_SECRET_KEY = "minioadmin";
    private static final String DEFAULT_BRANCH = "main";
    private static final String TEST_FILE_NAME = "test.txt";
    private static final String TEST_FILE_CONTENT = "Hello";
    private static final String FEATURE_BRANCH = "feature";

    private static Network network = Network.newNetwork();

    @Container
    private static GenericContainer<?> minioContainer = new GenericContainer<>("minio/minio:latest")
            .withNetwork(network)
            .withNetworkAliases("minio")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", MINIO_ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", MINIO_SECRET_KEY)
            .withCommand("server /data --console-address :9001")
            .waitingFor(Wait.forHttp("/minio/health/live").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(5)));

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
            .waitingFor(Wait.forHttp("/api/v1/setup_lakefs").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(10)))
            .dependsOn(minioContainer);

    @Autowired
    private LakeFSService lakeFSService;

    @Autowired
    private Map<String, RestTemplate> lakeFSClientMap;

    private LFSConfig config;
    private String tenant;
    private String repositoryName;
    private String storageNamespace;

    private String TEST_USER;
    private String TEST_GROUP;
    private String TEST_POLICY;

    @BeforeAll
    public static void setUpContainer() {
        try {
            minioContainer.start();
            log.info("MinIO container started. ID: {}, Status: {}", minioContainer.getContainerId(), minioContainer.getContainerInfo().getState().getStatus());
            lakeFSContainer.start();
            log.info("LakeFS container started. ID: {}, Status: {}", lakeFSContainer.getContainerId(), lakeFSContainer.getContainerInfo().getState().getStatus());
            Thread.sleep(60000); // Wait for LakeFS to be fully ready
            initializeLakeFS();
        } catch (Exception e) {
            log.error("Failed to start containers. MinIO logs: {}\nLakeFS logs: {}", minioContainer.getLogs(), lakeFSContainer.getLogs(), e);
            throw new RuntimeException("Container startup failed", e);
        }
    }

    private static void initializeLakeFS() {
        try {
            String lakeFSUrl = "http://" + lakeFSContainer.getHost() + ":" + lakeFSContainer.getMappedPort(8000);
            log.info("Initializing LakeFS at URL: {}", lakeFSUrl);
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> setupRequest = Map.of(
                    "username", "admin",
                    "key", Map.of("access_key_id", ACCESS_KEY, "secret_access_key", SECRET_KEY)
            );
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(setupRequest, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(lakeFSUrl + "/api/v1/setup_lakefs", entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("LakeFS setup completed successfully: {}", response.getBody());
            } else {
                throw new RuntimeException("LakeFS initialization failed: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to initialize LakeFS: {}", lakeFSContainer.getLogs(), e);
            throw new RuntimeException("LakeFS initialization failed", e);
        }
    }

    @BeforeEach
    public void setUp() throws InterruptedException {
        if (!minioContainer.isRunning() || !lakeFSContainer.isRunning()) {
            throw new RuntimeException("Containers are not running. MinIO logs: " + minioContainer.getLogs() + "\nLakeFS logs: " + lakeFSContainer.getLogs());
        }

        TEST_USER = UUID.randomUUID().toString();
        TEST_GROUP = UUID.randomUUID().toString();
        TEST_POLICY = UUID.randomUUID().toString();

        tenant = "tenant-" + UUID.randomUUID();
        repositoryName = "repo-" + UUID.randomUUID();
        storageNamespace = "bucket-" + UUID.randomUUID();

        config = new LFSConfig();
        config.setTenant(tenant);
        config.setUrl("http://" + lakeFSContainer.getHost() + ":" + lakeFSContainer.getMappedPort(8000) + "/api/v1");
        config.setUserName(ACCESS_KEY);
        config.setPassword(SECRET_KEY);
        config.setS3Config(S3Config.builder()
                .tenant(tenant)
                .url("http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000))
                .userName(MINIO_ACCESS_KEY)
                .password(MINIO_SECRET_KEY)
                .region("us-east-1")
                .build());

        // Retry connection to LakeFS
        int maxRetries = 10;
        Exception lastException = null;
        for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
            try {
                Thread.sleep(2000);
                lakeFSService.getConnection(config);
                log.info("Connected to LakeFS on attempt {}", retryCount + 1);
                return;
            } catch (Exception e) {
                lastException = e;
                log.warn("Connection attempt {} failed: {}", retryCount + 1, e.getMessage());
                if ((retryCount + 1) % 3 == 0) {
                    log.error("Container logs at attempt {}: MinIO: {}, LakeFS: {}", retryCount + 1, minioContainer.getLogs(), lakeFSContainer.getLogs());
                }
            }
        }
        throw new RuntimeException("Could not connect to LakeFS after " + maxRetries + " attempts", lastException);
    }

    @AfterEach
    public void tearDown() {
        try {
            List<String> repositories = lakeFSService.getRepositories(config);
            for (String repo : repositories) {
                try {
                    lakeFSService.deleteRepository(config, repo, true);
                    log.info("Cleaned up repository: {}", repo);
                } catch (Exception e) {
                    log.warn("Failed to delete repository {}: {}", repo, e.getMessage());
                }
            }

            lakeFSClientMap.remove(tenant);
        } catch (Exception e) {
            log.error("Failed to clean up repositories: {}", e.getMessage());
        }
    }

    private void createTestRepository() {
        lakeFSService.createRepository(config, repositoryName, storageNamespace, DEFAULT_BRANCH);
        assertTrue(lakeFSService.repositoryExists(config, repositoryName), "Repository should exist after creation");
    }

    private void uploadTestFile(String branch, String fileName, String content, Map<String, String> metadata) {
        MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain", content.getBytes());
        lakeFSService.uploadFile(config, repositoryName, branch, "", fileName, file);
        if (metadata != null) {
            lakeFSService.updateMetadata(config, repositoryName, branch, fileName, metadata);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should establish LakeFS connection")
    void testGetConnection() {
        RestTemplate result = lakeFSService.getConnection(config);
        assertNotNull(result, "Connection should not be null");
        assertSame(lakeFSClientMap.get(tenant), result, "Connection should be cached for tenant");
    }

    @Test
    @Order(2)
    @DisplayName("Should update LakeFS connection")
    void testUpdateConnection() {
        lakeFSService.updateConnection(config);
        assertNotNull(lakeFSClientMap.get(tenant), "Updated connection should exist in client map");
    }

    @Test
    @Order(3)
    @DisplayName("Should create a repository")
    void testCreateRepository() {
        assertDoesNotThrow(() -> lakeFSService.createRepository(config, repositoryName, storageNamespace, DEFAULT_BRANCH));
        assertTrue(lakeFSService.repositoryExists(config, repositoryName), "Repository should exist after creation");
    }

    @Test
    @Order(4)
    @DisplayName("Should verify repository existence")
    void testRepositoryExists() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        assertTrue(lakeFSService.repositoryExists(config, repositoryName), "Repository should exist");
        assertFalse(lakeFSService.repositoryExists(config, "nonexistent-repo"), "Non-existent repository should return false");
    }

    @Test
    @Order(5)
    @DisplayName("Should verify branch existence")
    void testBranchExists() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        assertTrue(lakeFSService.branchExists(config, repositoryName, DEFAULT_BRANCH), "Default branch should exist");
        assertFalse(lakeFSService.branchExists(config, repositoryName, "nonexistent-branch"), "Non-existent branch should return false");
    }

    @Test
    @Order(6)
    @DisplayName("Should create a new branch")
    void testCreateBranch() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        assertDoesNotThrow(() -> lakeFSService.createBranch(config, repositoryName, FEATURE_BRANCH, DEFAULT_BRANCH));
        assertTrue(lakeFSService.branchExists(config, repositoryName, FEATURE_BRANCH), "New branch should exist");
    }

    @Test
    @Order(7)
    @DisplayName("Should upload a file")
    void testUploadFile() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        MockMultipartFile file = new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", TEST_FILE_CONTENT.getBytes());
        assertDoesNotThrow(() -> lakeFSService.uploadFile(config, repositoryName, DEFAULT_BRANCH, "data", TEST_FILE_NAME, file));
    }

    @Test
    @Order(8)
    @DisplayName("Should retrieve an object")
    void testGetObject() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        uploadTestFile(DEFAULT_BRANCH, TEST_FILE_NAME, TEST_FILE_CONTENT, null);
        byte[] result = lakeFSService.getObject(config, repositoryName, DEFAULT_BRANCH, TEST_FILE_NAME);
        assertArrayEquals(TEST_FILE_CONTENT.getBytes(), result, "Retrieved file content should match");
    }

    //@Test
    @Order(9)
    @DisplayName("Should generate a presigned URL")
    void testGetPresignedObjectUrl() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        uploadTestFile(DEFAULT_BRANCH, TEST_FILE_NAME, TEST_FILE_CONTENT, null);
        String url = lakeFSService.getPresignedObjectUrl(config, repositoryName, DEFAULT_BRANCH, TEST_FILE_NAME, 1);
        assertNotNull(url, "Presigned URL should not be null");
        assertTrue(url.startsWith("http"), "Presigned URL should start with http");
    }

    @Test
    @Order(10)
    @DisplayName("Should delete an object")
    void testDeleteObject() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        uploadTestFile(DEFAULT_BRANCH, TEST_FILE_NAME, TEST_FILE_CONTENT, null);
        assertDoesNotThrow(() -> lakeFSService.deleteObject(config, repositoryName, DEFAULT_BRANCH, TEST_FILE_NAME));
        assertThrows(LakeFSException.class, () -> lakeFSService.getObject(config, repositoryName, DEFAULT_BRANCH, TEST_FILE_NAME),
                "Should throw exception for deleted object");
    }

    @Test
    @Order(11)
    @DisplayName("Should retrieve objects by metadata")
    void testGetObjectByMetadata() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        Map<String, String> metadata1 = Map.of("key", "value", "category", "test", "type", "document");
        Map<String, String> metadata2 = Map.of("key", "different", "category", "test", "type", "image");
        uploadTestFile(DEFAULT_BRANCH, "test1.txt", "Content 1", metadata1);
        uploadTestFile(DEFAULT_BRANCH, "test2.txt", "Content 2", metadata2);
        lakeFSService.commit(config, repositoryName, DEFAULT_BRANCH, "Add test files with metadata", null);
        Thread.sleep(2000);

        // Test AND condition
        List<FileStorage> objects = lakeFSService.getObjectByMetadata(config, repositoryName, DEFAULT_BRANCH,
                Map.of("key", "value", "category", "test"), IEnumLogicalOperator.Types.AND);
        assertEquals(1, objects.size(), "Should find one object with AND condition");
        assertEquals("test1.txt", objects.get(0).objectName, "Should find test1.txt");

        // Test OR condition
        objects = lakeFSService.getObjectByMetadata(config, repositoryName, DEFAULT_BRANCH,
                Map.of("category", "test"), IEnumLogicalOperator.Types.OR);
        assertEquals(2, objects.size(), "Should find both objects with OR condition");

        // Test non-matching metadata
        objects = lakeFSService.getObjectByMetadata(config, repositoryName, DEFAULT_BRANCH,
                Map.of("key", "nonexistent"), IEnumLogicalOperator.Types.AND);
        assertTrue(objects.isEmpty(), "Should find no objects with non-matching metadata");
    }

    @Test
    @Order(12)
    @DisplayName("Should handle metadata edge cases")
    void testGetObjectByMetadataEdgeCases() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        assertThrows(IllegalArgumentException.class, () ->
                        lakeFSService.getObjectByMetadata(config, repositoryName, DEFAULT_BRANCH, Collections.emptyMap(), IEnumLogicalOperator.Types.AND),
                "Should throw for empty metadata");
        assertThrows(IllegalArgumentException.class, () ->
                        lakeFSService.getObjectByMetadata(config, repositoryName, DEFAULT_BRANCH, null, IEnumLogicalOperator.Types.AND),
                "Should throw for null metadata");
        assertThrows(IllegalArgumentException.class, () ->
                        lakeFSService.getObjectByMetadata(config, repositoryName, DEFAULT_BRANCH, Map.of("key", "value"), null),
                "Should throw for null condition");
    }

    @Test
    @Order(13)
    @DisplayName("Should list objects")
    void testGetObjects() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        uploadTestFile(DEFAULT_BRANCH, TEST_FILE_NAME, TEST_FILE_CONTENT, null);
        lakeFSService.commit(config, repositoryName, DEFAULT_BRANCH, "Add test file", null);
        Thread.sleep(3000);

        List<FileStorage> objects = lakeFSService.getObjects(config, repositoryName, DEFAULT_BRANCH, "");
        assertEquals(1, objects.size(), "Should find one object");
        assertEquals(TEST_FILE_NAME, objects.get(0).objectName, "Should find the uploaded file");
    }

    @Test
    @Order(14)
    @DisplayName("Should update object metadata")
    void testUpdateMetadata() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        uploadTestFile(DEFAULT_BRANCH, TEST_FILE_NAME, TEST_FILE_CONTENT, null);
        Map<String, String> metadata = Map.of("key", "new-value");
        assertDoesNotThrow(() -> lakeFSService.updateMetadata(config, repositoryName, DEFAULT_BRANCH, TEST_FILE_NAME, metadata));
        List<FileStorage> objects = lakeFSService.getObjectByMetadata(config, repositoryName, DEFAULT_BRANCH, metadata, IEnumLogicalOperator.Types.AND);
        assertEquals(1, objects.size(), "Should find object with updated metadata");
    }

    @Test
    @Order(15)
    @DisplayName("Should delete multiple objects")
    void testDeleteObjects() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        uploadTestFile(DEFAULT_BRANCH, "test1.txt", "Hello1", null);
        uploadTestFile(DEFAULT_BRANCH, "test2.txt", "Hello2", null);
        assertDoesNotThrow(() -> lakeFSService.deleteObjects(config, repositoryName, DEFAULT_BRANCH, List.of("test1.txt", "test2.txt")));
        assertThrows(LakeFSException.class, () -> lakeFSService.getObject(config, repositoryName, DEFAULT_BRANCH, "test1.txt"),
                "Should throw for deleted object test1.txt");
    }

    @Test
    @Order(16)
    @DisplayName("Should commit changes")
    void testCommit() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        uploadTestFile(DEFAULT_BRANCH, TEST_FILE_NAME, TEST_FILE_CONTENT, null);
        String commitId = lakeFSService.commit(config, repositoryName, DEFAULT_BRANCH, "Test commit", Map.of("key", "value"));
        assertNotNull(commitId, "Commit ID should not be null");
    }

    @Test
    @Order(17)
    @DisplayName("Should fail commit with no changes")
    void testCommitNoChanges() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        assertThrows(LakeFSException.class, () ->
                        lakeFSService.commit(config, repositoryName, DEFAULT_BRANCH, "Test commit", Map.of("key", "value")),
                "Should throw for commit with no changes");
    }

    @Test
    @Order(18)
    @DisplayName("Should merge branches")
    void testMerge() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        lakeFSService.createBranch(config, repositoryName, FEATURE_BRANCH, DEFAULT_BRANCH);
        uploadTestFile(FEATURE_BRANCH, TEST_FILE_NAME, TEST_FILE_CONTENT, null);
        String commitId = lakeFSService.commit(config, repositoryName, FEATURE_BRANCH, "Add test file", null);
        assertNotNull(commitId, "Commit should succeed");
        Thread.sleep(3000);
        String mergeId = lakeFSService.merge(config, repositoryName, FEATURE_BRANCH, DEFAULT_BRANCH, "Test merge");
        assertNotNull(mergeId, "Merge should succeed");
        byte[] mergedContent = lakeFSService.getObject(config, repositoryName, DEFAULT_BRANCH, TEST_FILE_NAME);
        assertArrayEquals(TEST_FILE_CONTENT.getBytes(), mergedContent, "File should exist in main branch after merge");
    }

    @Test
    @Order(19)
    @DisplayName("Should fail merge with no changes")
    void testMergeNoChanges() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        lakeFSService.createBranch(config, repositoryName, FEATURE_BRANCH, DEFAULT_BRANCH);
        assertThrows(LakeFSException.class, () ->
                        lakeFSService.merge(config, repositoryName, FEATURE_BRANCH, DEFAULT_BRANCH, "Test merge"),
                "Should throw for merge with no changes");
    }

    @Test
    @Order(20)
    @DisplayName("Should list branches")
    void testGetBranches() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        List<String> branches = lakeFSService.getBranches(config, repositoryName);
        assertEquals(1, branches.size(), "Should find one branch");
        assertTrue(branches.contains(DEFAULT_BRANCH), "Should contain default branch");
    }

    @Test
    @Order(21)
    @DisplayName("Should list repositories")
    void testGetRepositories() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        List<String> repositories = lakeFSService.getRepositories(config);
        assertTrue(repositories.contains(repositoryName), "Should contain created repository");
    }

    @Test
    @Order(22)
    @DisplayName("Should delete a branch")
    void testDeleteBranch() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        lakeFSService.createBranch(config, repositoryName, FEATURE_BRANCH, DEFAULT_BRANCH);
        assertDoesNotThrow(() -> lakeFSService.deleteBranch(config, repositoryName, FEATURE_BRANCH));
        assertFalse(lakeFSService.branchExists(config, repositoryName, FEATURE_BRANCH), "Branch should not exist after deletion");
    }

    //@Test
    @Order(23)
    @DisplayName("Should delete a repository")
    void testDeleteRepository() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        assertDoesNotThrow(() -> lakeFSService.deleteRepository(config, repositoryName, true));
        Thread.sleep(3000);
        assertFalse(lakeFSService.repositoryExists(config, repositoryName), "Repository should not exist after deletion");
    }

    @Test
    @Order(24)
    @DisplayName("Should retrieve commit history")
    void testGetCommitHistory() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        uploadTestFile(DEFAULT_BRANCH, TEST_FILE_NAME, TEST_FILE_CONTENT, null);
        lakeFSService.commit(config, repositoryName, DEFAULT_BRANCH, "Initial commit", null);
        List<Map<String, Object>> commits = lakeFSService.getCommitHistory(config, repositoryName, DEFAULT_BRANCH, 2);
        assertFalse(commits.isEmpty(), "Commit history should not be empty");
    }

    @Test
    @Order(25)
    @DisplayName("Should retrieve differences between branches")
    void testGetDiff() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        lakeFSService.createBranch(config, repositoryName, FEATURE_BRANCH, DEFAULT_BRANCH);
        uploadTestFile(FEATURE_BRANCH, TEST_FILE_NAME, TEST_FILE_CONTENT, null);
        lakeFSService.commit(config, repositoryName, FEATURE_BRANCH, "Add test file", null);
        Thread.sleep(3000);
        List<Map<String, Object>> diffs = lakeFSService.getDiff(config, repositoryName, DEFAULT_BRANCH, FEATURE_BRANCH);
        assertFalse(diffs.isEmpty(), "Should find differences");
        assertTrue(diffs.stream().anyMatch(diff -> diff.get("path").equals(TEST_FILE_NAME)),
                "Diff should contain test.txt");
    }

    @Test
    @Order(26)
    @DisplayName("Should revert to a commit")
    void testRevert() throws InterruptedException {
        createTestRepository();
        Thread.sleep(3000);
        uploadTestFile(DEFAULT_BRANCH, TEST_FILE_NAME, TEST_FILE_CONTENT, null);
        String commitId = lakeFSService.commit(config, repositoryName, DEFAULT_BRANCH, "Initial commit", null);
        assertDoesNotThrow(() -> lakeFSService.revert(config, repositoryName, DEFAULT_BRANCH, commitId));
        assertThrows(LakeFSException.class, () -> lakeFSService.getObject(config, repositoryName, DEFAULT_BRANCH, TEST_FILE_NAME),
                "File should not exist after revert");
    }

    //@Test
    @Order(27)
    @DisplayName("Should create and retrieve a user")
    void testCreateAndGetUser() {
        assertDoesNotThrow(() -> lakeFSService.createUser(config, TEST_USER));
        Map<String, Object> user = lakeFSService.getUser(config, TEST_USER);
        assertNotNull(user, "User should be retrieved");
        assertEquals(TEST_USER, user.get("id"), "User ID should match");
    }

    //@Test
    @Order(28)
    @DisplayName("Should create and retrieve a group")
    void testCreateAndGetGroup() {
        assertDoesNotThrow(() -> lakeFSService.createGroup(config, TEST_GROUP));
        List<String> groups = lakeFSService.listGroups(config, null, 100);
        assertTrue(groups.contains(TEST_GROUP), "Group should be listed");
    }

    //@Test
    @Order(29)
    @DisplayName("Should create and retrieve a policy")
    void testCreateAndGetPolicy() {
        List<Map<String, Object>> statement = List.of(Map.of(
                "effect", "allow",
                "action", List.of("fs:Read"),
                "resource", "arn:lakefs:fs:::test-repo/*"
        ));
        assertDoesNotThrow(() -> lakeFSService.createPolicy(config, TEST_POLICY, statement));
        Map<String, Object> policy = lakeFSService.getPolicy(config, TEST_POLICY);
        assertNotNull(policy, "Policy should be retrieved");
        assertEquals(TEST_POLICY, policy.get("id"), "Policy ID should match");
    }

    //@Test
    @Order(30)
    @DisplayName("Should attach and detach policy to/from group")
    void testAttachAndDetachPolicyToGroup() {
        lakeFSService.createGroup(config, TEST_GROUP);
        List<Map<String, Object>> statement = List.of(Map.of(
                "effect", "allow",
                "action", List.of("fs:Read"),
                "resource", "arn:lakefs:fs:::test-repo/*"
        ));
        lakeFSService.createPolicy(config, TEST_POLICY, statement);

        assertDoesNotThrow(() -> lakeFSService.attachPolicyToGroup(config, TEST_GROUP, TEST_POLICY));
        List<String> policies = lakeFSService.listGroupPolicies(config, TEST_GROUP, null, 100);
        assertTrue(policies.contains(TEST_POLICY), "Policy should be attached to group");

        assertDoesNotThrow(() -> lakeFSService.detachPolicyFromGroup(config, TEST_GROUP, TEST_POLICY));
        policies = lakeFSService.listGroupPolicies(config, TEST_GROUP, null, 100);
        assertFalse(policies.contains(TEST_POLICY), "Policy should be detached from group");
    }

    //@Test
    @Order(31)
    @DisplayName("Should add and remove group member")
    void testAddAndRemoveGroupMember() {
        lakeFSService.createUser(config, TEST_USER);
        lakeFSService.createGroup(config, TEST_GROUP);

        assertDoesNotThrow(() -> lakeFSService.addGroupMember(config, TEST_GROUP, TEST_USER));
        List<String> members = lakeFSService.listGroupMembers(config, TEST_GROUP, null, 100);
        assertTrue(members.contains(TEST_USER), "User should be added to group");

        assertDoesNotThrow(() -> lakeFSService.removeGroupMember(config, TEST_GROUP, TEST_USER));
        members = lakeFSService.listGroupMembers(config, TEST_GROUP, null, 100);
        assertFalse(members.contains(TEST_USER), "User should be removed from group");
    }

    //@Test
    @Order(32)
    @DisplayName("Should delete user, group, and policy")
    void testDeleteAuthEntities() {
        lakeFSService.createUser(config, TEST_USER);
        lakeFSService.createGroup(config, TEST_GROUP);
        List<Map<String, Object>> statement = List.of(Map.of(
                "effect", "allow",
                "action", List.of("fs:Read"),
                "resource", "arn:lakefs:fs:::test-repo/*"
        ));
        lakeFSService.createPolicy(config, TEST_POLICY, statement);

        assertDoesNotThrow(() -> lakeFSService.deleteUser(config, TEST_USER));
        assertThrows(LakeFSException.class, () -> lakeFSService.getUser(config, TEST_USER), "Should throw for deleted user");

        assertDoesNotThrow(() -> lakeFSService.deleteGroup(config, TEST_GROUP));
        List<String> groups = lakeFSService.listGroups(config, null, 100);
        assertFalse(groups.contains(TEST_GROUP), "Group should be deleted");

        assertDoesNotThrow(() -> lakeFSService.deletePolicy(config, TEST_POLICY));
        assertThrows(LakeFSException.class, () -> lakeFSService.getPolicy(config, TEST_POLICY), "Should throw for deleted policy");
    }
}