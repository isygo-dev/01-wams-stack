package eu.isygoit.storage.s3;


import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.exception.LakeFSObjectException;
import eu.isygoit.storage.lfs.api.impl.LakeFSApiService;
import eu.isygoit.storage.s3.object.FileStorage;
import eu.isygoit.storage.s3.object.StorageConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Testcontainers
public class LFSStorageApplicationTest {

    private LakeFSApiService lakeFSApiService;
    private RestTemplate restTemplate;
    private StorageConfig config;
    private Map<String, RestTemplate> lakeFSClientMap;

    // Assuming a LakeFS container is available; adjust image as needed
    @Container
    private GenericContainer<?> lakeFSContainer = new GenericContainer<>("treeverse/lakefs:latest")
            .withExposedPorts(8000)
            .withEnv("LAKEFS_AUTH_USERNAME", "admin")
            .withEnv("LAKEFS_AUTH_ACCESS_KEY_ID", "AKIAIOSFODNN7EXAMPLE")
            .withEnv("LAKEFS_AUTH_SECRET_ACCESS_KEY", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
            .withEnv("LAKEFS_BLOCKSTORE_TYPE", "local")
            .withEnv("LAKEFS_BLOCKSTORE_LOCAL_PATH", "/lakefs/data")
            .withCommand("run", "--quickstart")
            .withFileSystemBind("./lakefs-data", "/lakefs/data"); // Local directory for persistence

    @BeforeEach
    public void setUp() {
        // Initialize mocks
        restTemplate = mock(RestTemplate.class);
        lakeFSClientMap = new HashMap<>();
        lakeFSClientMap.put("test-tenant", restTemplate);
        lakeFSApiService = new LakeFSApiService(lakeFSClientMap) {
            // Anonymous class to instantiate abstract class
        };

        // Configure StorageConfig
        config = new StorageConfig();
        config.setTenant("test-tenant");
        config.setUrl("http://localhost:" + lakeFSContainer.getMappedPort(8000) + "/api/v1");
        config.setUserName("admin");
        config.setPassword("admin");
    }

    @Test
    public void testGetConnection() {
        RestTemplate result = lakeFSApiService.getConnection(config);
        assertNotNull(result);
        assertEquals(restTemplate, result);
    }

    @Test
    public void testUpdateConnection() {
        assertDoesNotThrow(() -> lakeFSApiService.updateConnection(config));
        assertEquals(restTemplate, lakeFSClientMap.get("test-tenant"));
    }

    @Test
    public void testRepositoryExists_Success() {
        String repositoryName = "test-repo";
        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        boolean exists = lakeFSApiService.repositoryExists(config, repositoryName);
        assertTrue(exists);
    }

    @Test
    public void testRepositoryExists_NotFound() {
        String repositoryName = "test-repo";
        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenThrow(new RuntimeException("404"));

        boolean exists = lakeFSApiService.repositoryExists(config, repositoryName);
        assertFalse(exists);
    }

    @Test
    public void testCreateRepository() {
        String repositoryName = "test-repo";
        String storageNamespace = "s3://test-bucket";
        String defaultBranch = "main";

        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenThrow(new RuntimeException("404")); // Repo doesn't exist
        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        assertDoesNotThrow(() -> lakeFSApiService.createRepository(config, repositoryName, storageNamespace, defaultBranch));
    }

    @Test
    public void testBranchExists_Success() {
        String repositoryName = "test-repo";
        String branchName = "main";
        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        boolean exists = lakeFSApiService.branchExists(config, repositoryName, branchName);
        assertTrue(exists);
    }

    @Test
    public void testCreateBranch() {
        String repositoryName = "test-repo";
        String branchName = "feature";
        String sourceBranch = "main";

        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenThrow(new RuntimeException("404")); // Branch doesn't exist
        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        assertDoesNotThrow(() -> lakeFSApiService.createBranch(config, repositoryName, branchName, sourceBranch));
    }

    @Test
    public void testUploadFile() {
        String repositoryName = "test-repo";
        String branchName = "main";
        String path = "data";
        String objectName = "test.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
        Map<String, String> metadata = Map.of("key", "value");

        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertDoesNotThrow(() -> lakeFSApiService.uploadFile(config, repositoryName, branchName, path, objectName, multipartFile, metadata));
    }

    @Test
    public void testGetObject() {
        String repositoryName = "test-repo";
        String reference = "main";
        String objectName = "test.txt";
        byte[] expectedContent = "Hello".getBytes();

        when(restTemplate.getForEntity(any(String.class), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>(expectedContent, HttpStatus.OK));

        byte[] result = lakeFSApiService.getObject(config, repositoryName, reference, objectName);
        assertArrayEquals(expectedContent, result);
    }

    @Test
    public void testGetPresignedObjectUrl() {
        String repositoryName = "test-repo";
        String reference = "main";
        String objectName = "test.txt";
        Map<String, Object> responseBody = Map.of("url", "http://presigned.url");

        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        String url = lakeFSApiService.getPresignedObjectUrl(config, repositoryName, reference, objectName);
        assertEquals("http://presigned.url", url);
    }

    @Test
    public void testDeleteObject() {
        String repositoryName = "test-repo";
        String branchName = "main";
        String objectName = "test.txt";

        doNothing().when(restTemplate).delete(any(String.class));

        assertDoesNotThrow(() -> lakeFSApiService.deleteObject(config, repositoryName, branchName, objectName));
    }

    @Test
    public void testGetObjectByMetadata() {
        String repositoryName = "test-validated-repo";
        String reference = "main";
        Map<String, String> metadata = Map.of("key", "value");
        List<Map<String, Object>> results = List.of(
                Map.of(
                        "path", "test.txt",
                        "size_bytes", 100,
                        "checksum", "abc123",
                        "mtime", "2023-10-01T12:00:00Z",
                        "metadata", Map.of("key", "value"),
                        "path_type", "object"
                )
        );
        Map<String, Object> responseBody = Map.of("results", results);

        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        List<FileStorage> objects = lakeFSApiService.getObjectByMetadata(config, repositoryName, reference, metadata, IEnumLogicalOperator.Types.AND);
        assertFalse(objects.isEmpty());
        assertEquals("test.txt", objects.get(0).objectName);
    }

    @Test
    public void testGetObjects() {
        String repositoryName = "test-validated-repo";
        String reference = "main";
        List<Map<String, Object>> results = List.of(
                Map.of(
                        "path", "test.txt",
                        "size_bytes", 100,
                        "checksum", "abc123",
                        "mtime", "2023-10-01T12:00:00Z",
                        "metadata", Map.of("key", "value"),
                        "path_type", "object"
                )
        );
        Map<String, Object> responseBody = Map.of("results", results);

        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        List<FileStorage> objects = lakeFSApiService.getObjects(config, repositoryName, reference, null);
        assertFalse(objects.isEmpty());
        assertEquals("test.txt", objects.get(0).objectName);
    }

    @Test
    public void testUpdateMetadata() {
        String repositoryName = "test-repo";
        String branchName = "main";
        String objectName = "test.txt";
        Map<String, String> metadata = Map.of("key", "new-value");

        doNothing().when(restTemplate).put(any(String.class), any(HttpEntity.class));

        assertDoesNotThrow(() -> lakeFSApiService.updateMetadata(config, repositoryName, branchName, objectName, metadata));
    }

    @Test
    public void testDeleteObjects() {
        String repositoryName = "test-repo";
        String branchName = "main";
        List<String> objectNames = List.of("test1.txt", "test2.txt");

        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertDoesNotThrow(() -> lakeFSApiService.deleteObjects(config, repositoryName, branchName, objectNames));
    }

    @Test
    public void testCommit() {
        String repositoryName = "test-repo";
        String branchName = "main";
        String message = "Test commit";
        Map<String, String> metadata = Map.of("key", "value");
        Map<String, Object> responseBody = Map.of("id", "commit123");

        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        String commitId = lakeFSApiService.commit(config, repositoryName, branchName, message, metadata);
        assertEquals("commit123", commitId);
    }

    @Test
    public void testMerge() {
        String repositoryName = "test-repo";
        String sourceBranch = "feature";
        String destBranch = "main";
        String message = "Merge feature into main";
        Map<String, Object> responseBody = Map.of("reference", "merge123");

        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        String mergeCommitId = lakeFSApiService.merge(config, repositoryName, sourceBranch, destBranch, message);
        assertEquals("merge123", mergeCommitId);
    }

    @Test
    public void testGetBranches() {
        String repositoryName = "test-validated-repo";
        Map<String, Object> responseBody = Map.of("results", List.of(
                Map.of("id", "main"),
                Map.of("id", "feature")
        ));

        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        List<String> branches = lakeFSApiService.getBranches(config, repositoryName);
        assertEquals(2, branches.size());
        assertTrue(branches.contains("main"));
        assertTrue(branches.contains("feature"));
    }

    @Test
    public void testGetRepositories() {
        Map<String, Object> responseBody = Map.of("results", List.of(
                Map.of("id", "repo1"),
                Map.of("id", "repo2")
        ));

        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        List<String> repositories = lakeFSApiService.getRepositories(config);
        assertEquals(2, repositories.size());
        assertTrue(repositories.contains("repo1"));
        assertTrue(repositories.contains("repo2"));
    }

    @Test
    public void testGetCommitHistory() {
        String repositoryName = "test-validated-repo";
        String branchName = "main";
        int limit = 2;
        Map<String, Object> responseBody = Map.of("results", List.of(
                Map.of("id", "commit1"),
                Map.of("id", "commit2")
        ));

        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        List<Map<String, Object>> commits = lakeFSApiService.getCommitHistory(config, repositoryName, branchName, limit);
        assertEquals(2, commits.size());
    }

    @Test
    public void testGetDiff() {
        String repositoryName = "test-validated-repo";
        String leftRef = "main";
        String rightRef = "feature";
        Map<String, Object> responseBody = Map.of("results", List.of(
                Map.of("path", "test.txt", "type", "added")
        ));

        when(restTemplate.getForEntity(any(String.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        List<Map<String, Object>> diffs = lakeFSApiService.getDiff(config, repositoryName, leftRef, rightRef);
        assertEquals(1, diffs.size());
        assertEquals("test.txt", diffs.get(0).get("path"));
    }

    @Test
    public void testRevert() {
        String repositoryName = "test-repo";
        String branchName = "main";
        String commitId = "commit123";

        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertDoesNotThrow(() -> lakeFSApiService.revert(config, repositoryName, branchName, commitId));
    }

    @Test
    public void testDeleteBranch() {
        String repositoryName = "test-repo";
        String branchName = "feature";

        doNothing().when(restTemplate).delete(any(String.class));

        assertDoesNotThrow(() -> lakeFSApiService.deleteBranch(config, repositoryName, branchName));
    }

    @Test
    public void testDeleteRepository() {
        String repositoryName = "test-repo";

        doNothing().when(restTemplate).delete(any(String.class));

        assertDoesNotThrow(() -> lakeFSApiService.deleteRepository(config, repositoryName));
    }
}
