package eu.isygoit.storage.lfs.api.impl;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.lfs.api.ILakeFSApiService;
import eu.isygoit.storage.exception.LakeFSObjectException;
import eu.isygoit.storage.s3.object.FileStorage;
import eu.isygoit.storage.s3.object.StorageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for LakeFS data versioning operations with enhanced error handling,
 * connection pooling, and retry logic.
 */
@Slf4j
public abstract class LakeFSApiService implements ILakeFSApiService {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int DEFAULT_PRESIGNED_URL_EXPIRY_HOURS = 2;
    private static final String DEFAULT_BRANCH = "main";

    private final Map<String, RestTemplate> lakeFSClientMap;

    /**
     * Instantiates a new Lake fs api service.
     *
     * @param lakeFSClientMap the lake fs client map
     */
    public LakeFSApiService(Map<String, RestTemplate> lakeFSClientMap) {
        this.lakeFSClientMap = lakeFSClientMap;
    }

    /**
     * Retrieves or creates a LakeFS client connection for the specified tenant.
     *
     * @param config Storage configuration containing tenant, credentials, and endpoint
     * @return RestTemplate configured for LakeFS API calls
     * @throws IllegalArgumentException if config is invalid
     */
    @Override
    public RestTemplate getConnection(StorageConfig config) {
        validateConfig(config);
        return lakeFSClientMap.computeIfAbsent(config.getTenant(), k -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                // Configure basic authentication
                restTemplate.getInterceptors().add((request, body, execution) -> {
                    String auth = config.getUserName() + ":" + config.getPassword();
                    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
                    String authHeader = "Basic " + new String(encodedAuth);
                    request.getHeaders().add("Authorization", authHeader);
                    return execution.execute(request, body);
                });
                return restTemplate;
            } catch (Exception e) {
                log.error("Failed to create LakeFS client for tenant: {}", config.getTenant(), e);
                throw new LakeFSObjectException("Failed to initialize LakeFS client", e);
            }
        });
    }

    /**
     * Updates the LakeFS client connection for a tenant.
     *
     * @param config Storage configuration
     * @throws IllegalArgumentException if config is invalid
     */
    @Override
    public void updateConnection(StorageConfig config) {
        validateConfig(config);
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getInterceptors().add((request, body, execution) -> {
                String auth = config.getUserName() + ":" + config.getPassword();
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
                String authHeader = "Basic " + new String(encodedAuth);
                request.getHeaders().add("Authorization", authHeader);
                return execution.execute(request, body);
            });
            lakeFSClientMap.put(config.getTenant(), restTemplate);
            log.info("Updated LakeFS connection for tenant: {}", config.getTenant());
        } catch (Exception e) {
            log.error("Failed to update LakeFS connection for tenant: {}", config.getTenant(), e);
            throw new LakeFSObjectException("Failed to update LakeFS connection", e);
        }
    }

    /**
     * Checks if a repository exists.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @return true if repository exists
     * @throws LakeFSObjectException on failure
     */
    @Override
    public boolean repositoryExists(StorageConfig config, String repositoryName) {
        validateRepositoryName(repositoryName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName;
                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                return response.getStatusCode() == HttpStatus.OK;
            } catch (Exception e) {
                if (e.getMessage().contains("404")) {
                    return false;
                }
                throw new LakeFSObjectException("Error checking repository existence", e);
            }
        });
    }

    /**
     * Creates a repository if it doesn't exist.
     *
     * @param config           Storage configuration
     * @param repositoryName   Name of the repository
     * @param storageNamespace Storage namespace
     * @param defaultBranch    Default branch name
     * @throws LakeFSObjectException if repository creation fails
     */
    @Override
    public void createRepository(StorageConfig config, String repositoryName, String storageNamespace, String defaultBranch) {
        validateRepositoryName(repositoryName);
        if (!StringUtils.hasText(storageNamespace)) {
            throw new IllegalArgumentException("Storage namespace cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                if (!repositoryExists(config, repositoryName)) {
                    RestTemplate client = getConnection(config);
                    String url = config.getUrl() + "/repositories";

                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("name", repositoryName);
                    requestBody.put("storage_namespace", storageNamespace);
                    requestBody.put("default_branch", StringUtils.hasText(defaultBranch) ? defaultBranch : DEFAULT_BRANCH);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                    client.postForEntity(url, request, Map.class);
                    log.info("Created repository: {}", repositoryName);
                }
            } catch (Exception e) {
                throw new LakeFSObjectException("Error creating repository: " + repositoryName, e);
            }
            return null;
        });
    }

    /**
     * Deletes multiple objects from a branch.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param objectNames    List of object names to delete
     * @throws LakeFSObjectException if deletion fails
     */
    @Override
    public void deleteObjects(StorageConfig config, String repositoryName, String branchName, List<String> objectNames) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        if (objectNames == null || objectNames.isEmpty()) {
            throw new IllegalArgumentException("Object names list cannot be null or empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/branches/" + branchName + "/objects/delete";

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("paths", objectNames);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Deleted {} objects from branch: {} in repository: {}", objectNames.size(), branchName, repositoryName);
            } catch (Exception e) {
                throw new LakeFSObjectException("Error deleting objects from repository: " + repositoryName, e);
            }
            return null;
        });
    }

    /**
     * Commits changes to a branch.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param message        Commit message
     * @param metadata       Commit metadata (optional)
     * @return Commit ID
     * @throws LakeFSObjectException if commit fails
     */
    @Override
    public String commit(StorageConfig config, String repositoryName, String branchName, String message, Map<String, String> metadata) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Commit message cannot be empty");
        }
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/branches/" + branchName + "/commits";

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("message", message);
                if (metadata != null) {
                    requestBody.put("metadata", metadata);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map> response = client.postForEntity(url, request, Map.class);
                Map<String, Object> responseBody = response.getBody();
                String commitId = (String) responseBody.get("id");
                log.info("Committed changes to branch: {} in repository: {}, commit ID: {}", branchName, repositoryName, commitId);
                return commitId;
            } catch (Exception e) {
                throw new LakeFSObjectException("Error committing changes to branch: " + branchName, e);
            }
        });
    }

    /**
     * Merges a source branch into a destination branch.
     *
     * @param config                Storage configuration
     * @param repositoryName        Name of the repository
     * @param sourceBranchName      Source branch name
     * @param destinationBranchName Destination branch name
     * @param message               Merge message
     * @return Merge commit ID
     * @throws LakeFSObjectException if merge fails
     */
    @Override
    public String merge(StorageConfig config, String repositoryName, String sourceBranchName, String destinationBranchName, String message) {
        validateRepositoryName(repositoryName);
        validateBranchName(sourceBranchName);
        validateBranchName(destinationBranchName);
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Merge message cannot be empty");
        }
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/refs/" + destinationBranchName + "/merge/" + sourceBranchName;

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("message", message);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map> response = client.postForEntity(url, request, Map.class);
                Map<String, Object> responseBody = response.getBody();
                String mergeCommitId = (String) responseBody.get("reference");
                log.info("Merged branch: {} into: {} in repository: {}, merge commit: {}", sourceBranchName, destinationBranchName, repositoryName, mergeCommitId);
                return mergeCommitId;
            } catch (Exception e) {
                throw new LakeFSObjectException("Error merging branch: " + sourceBranchName + " into: " + destinationBranchName, e);
            }
        });
    }

    /**
     * Lists all branches in a repository.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @return List of branch names
     * @throws LakeFSObjectException if listing fails
     */
    @Override
    public List<String> getBranches(StorageConfig config, String repositoryName) {
        validateRepositoryName(repositoryName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/branches";

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");

                return results.stream()
                        .map(branch -> (String) branch.get("id"))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new LakeFSObjectException("Error listing branches in repository: " + repositoryName, e);
            }
        });
    }

    /**
     * Lists all repositories for the given configuration.
     *
     * @param config Storage configuration
     * @return List of repository names
     * @throws LakeFSObjectException if listing fails
     */
    @Override
    public List<String> getRepositories(StorageConfig config) {
        validateConfig(config);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories";

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");

                List<String> repositories = results.stream()
                        .map(repo -> (String) repo.get("id"))
                        .collect(Collectors.toList());
                log.info("Retrieved {} repositories for tenant: {}", repositories.size(), config.getTenant());
                return repositories;
            } catch (Exception e) {
                throw new LakeFSObjectException("Error listing repositories", e);
            }
        });
    }

    /**
     * Gets the commit history for a branch.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param limit          Maximum number of commits to return
     * @return List of commit information
     * @throws LakeFSObjectException if retrieval fails
     */
    @Override
    public List<Map<String, Object>> getCommitHistory(StorageConfig config, String repositoryName, String branchName, int limit) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/refs/" + branchName + "/commits";
                if (limit > 0) {
                    url += "?amount=" + limit;
                }

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = response.getBody();
                return (List<Map<String, Object>>) responseBody.get("results");
            } catch (Exception e) {
                throw new LakeFSObjectException("Error retrieving commit history for branch: " + branchName, e);
            }
        });
    }

    /**
     * Gets the differences between two references (branches or commits).
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param leftRef        Left reference (branch or commit)
     * @param rightRef       Right reference (branch or commit)
     * @return List of differences
     * @throws LakeFSObjectException if retrieval fails
     */
    @Override
    public List<Map<String, Object>> getDiff(StorageConfig config, String repositoryName, String leftRef, String rightRef) {
        validateRepositoryName(repositoryName);
        validateBranchName(leftRef);
        validateBranchName(rightRef);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/refs/" + leftRef + "/diff/" + rightRef;

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = response.getBody();
                return (List<Map<String, Object>>) responseBody.get("results");
            } catch (Exception e) {
                throw new LakeFSObjectException("Error retrieving diff between: " + leftRef + " and: " + rightRef, e);
            }
        });
    }

    /**
     * Reverts changes in a branch to a specific commit.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param commitId       Commit ID to revert to
     * @throws LakeFSObjectException if revert fails
     */
    @Override
    public void revert(StorageConfig config, String repositoryName, String branchName, String commitId) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        if (!StringUtils.hasText(commitId)) {
            throw new IllegalArgumentException("Commit ID cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/branches/" + branchName + "/revert";

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("ref", commitId);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Reverted branch: {} to commit: {} in repository: {}", branchName, commitId, repositoryName);
            } catch (Exception e) {
                throw new LakeFSObjectException("Error reverting branch: " + branchName + " to commit: " + commitId, e);
            }
            return null;
        });
    }

    /**
     * Deletes a branch from a repository.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @throws LakeFSObjectException if branch deletion fails
     */
    @Override
    public void deleteBranch(StorageConfig config, String repositoryName, String branchName) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/branches/" + branchName;

                client.delete(url);
                log.info("Deleted branch: {} from repository: {}", branchName, repositoryName);
            } catch (Exception e) {
                throw new LakeFSObjectException("Error deleting branch: " + branchName, e);
            }
            return null;
        });
    }

    /**
     * Deletes a repository.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @throws LakeFSObjectException if repository deletion fails
     */
    @Override
    public void deleteRepository(StorageConfig config, String repositoryName) {
        validateRepositoryName(repositoryName);
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName;

                client.delete(url);
                log.info("Deleted repository: {}", repositoryName);
            } catch (Exception e) {
                throw new LakeFSObjectException("Error deleting repository: " + repositoryName, e);
            }
            return null;
        });
    }

    /**
     * Executes an operation with retry logic.
     *
     * @param operation The operation to execute
     * @param <T>       Return type
     * @return Operation result
     * @throws LakeFSObjectException on failure after retries
     */
    private <T> T executeWithRetry(Supplier<T> operation) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (LakeFSObjectException e) {
                attempt++;
                if (attempt == MAX_RETRIES) {
                    throw e;
                }
                try {
                    Thread.sleep((long) RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LakeFSObjectException("Retry interrupted", ie);
                }
                log.warn("Retrying operation, attempt {}/{}", attempt, MAX_RETRIES);
            }
        }
        throw new LakeFSObjectException("Operation failed after maximum retries");
    }

    /**
     * Validates storage configuration.
     *
     * @param config Storage configuration
     * @throws IllegalArgumentException if invalid
     */
    private void validateConfig(StorageConfig config) {
        if (config == null || !StringUtils.hasText(config.getTenant()) ||
                !StringUtils.hasText(config.getUrl()) ||
                !StringUtils.hasText(config.getUserName()) ||
                !StringUtils.hasText(config.getPassword())) {
            throw new IllegalArgumentException("Invalid storage configuration");
        }
    }

    /**
     * Validates repository name.
     *
     * @param repositoryName Name of the repository
     * @throws IllegalArgumentException if invalid
     */
    private void validateRepositoryName(String repositoryName) {
        if (!StringUtils.hasText(repositoryName)) {
            throw new IllegalArgumentException("Repository name cannot be empty");
        }
    }

    /**
     * Validates branch name.
     *
     * @param branchName Name of the branch
     * @throws IllegalArgumentException if invalid
     */
    private void validateBranchName(String branchName) {
        if (!StringUtils.hasText(branchName)) {
            throw new IllegalArgumentException("Branch name cannot be empty");
        }
    }

    /**
     * Validates object parameters.
     *
     * @param repositoryName Name of the repository
     * @param reference      Branch name or commit ID
     * @param objectName     Object name
     * @throws IllegalArgumentException if invalid
     */
    private void validateObjectParams(String repositoryName, String reference, String objectName) {
        validateRepositoryName(repositoryName);
        validateBranchName(reference);
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("Object name cannot be empty");
        }
    }

    /**
     * Validates upload parameters.
     *
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param objectName     Object name
     * @param multipartFile  File to upload
     * @throws IllegalArgumentException if invalid
     */
    private void validateUploadParams(String repositoryName, String branchName, String objectName, MultipartFile multipartFile) {
        validateObjectParams(repositoryName, branchName, objectName);
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("Multipart file cannot be null or empty");
        }
    }

    /**
     * Checks if a branch exists in a repository.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @return true if branch exists
     * @throws LakeFSObjectException on failure
     */
    @Override
    public boolean branchExists(StorageConfig config, String repositoryName, String branchName) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/branches/" + branchName;
                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                return response.getStatusCode() == HttpStatus.OK;
            } catch (Exception e) {
                if (e.getMessage().contains("404")) {
                    return false;
                }
                throw new LakeFSObjectException("Error checking branch existence", e);
            }
        });
    }

    /**
     * Creates a new branch from an existing branch.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the new branch
     * @param sourceBranch   Source branch to create from
     * @throws LakeFSObjectException if branch creation fails
     */
    @Override
    public void createBranch(StorageConfig config, String repositoryName, String branchName, String sourceBranch) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        validateBranchName(sourceBranch);
        executeWithRetry(() -> {
            try {
                if (!branchExists(config, repositoryName, branchName)) {
                    RestTemplate client = getConnection(config);
                    String url = config.getUrl() + "/repositories/" + repositoryName + "/branches";

                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("name", branchName);
                    requestBody.put("source", sourceBranch);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                    client.postForEntity(url, request, Map.class);
                    log.info("Created branch: {} from source: {} in repository: {}", branchName, sourceBranch, repositoryName);
                }
            } catch (Exception e) {
                throw new LakeFSObjectException("Error creating branch: " + branchName, e);
            }
            return null;
        });
    }

    /**
     * Uploads a file to LakeFS with metadata.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param path           Object path
     * @param objectName     Object name
     * @param multipartFile  File to upload
     * @param metadata       Metadata tags
     * @throws LakeFSObjectException if upload fails
     */
    @Override
    public void uploadFile(StorageConfig config, String repositoryName, String branchName, String path, String objectName,
                           MultipartFile multipartFile, Map<String, String> metadata) {
        validateUploadParams(repositoryName, branchName, objectName, multipartFile);
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String fullPath = StringUtils.hasText(path) ? path + "/" + objectName : objectName;
                String url = config.getUrl() + "/repositories/" + repositoryName + "/branches/" + branchName + "/objects?path=" + URLEncoder.encode(fullPath, StandardCharsets.UTF_8);

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("content", new InputStreamResource(multipartFile.getInputStream()));

                if (metadata != null) {
                    metadata.forEach(body::add);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Uploaded file: {} to branch: {} in repository: {}", fullPath, branchName, repositoryName);
            } catch (Exception e) {
                throw new LakeFSObjectException("Error uploading file: " + objectName, e);
            }
            return null;
        });
    }

    /**
     * Retrieves an object from LakeFS.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param reference      Branch name or commit ID
     * @param objectName     Object name
     * @return Object content as byte array
     * @throws LakeFSObjectException if retrieval fails
     */
    @Override
    public byte[] getObject(StorageConfig config, String repositoryName, String reference, String objectName) {
        validateObjectParams(repositoryName, reference, objectName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/refs/" + reference + "/objects?path=" + URLEncoder.encode(objectName, StandardCharsets.UTF_8);

                ResponseEntity<byte[]> response = client.getForEntity(url, byte[].class);
                return response.getBody();
            } catch (Exception e) {
                throw new LakeFSObjectException("Error retrieving object: " + objectName, e);
            }
        });
    }

    /**
     * Generates a presigned URL for an object.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param reference      Branch name or commit ID
     * @param objectName     Object name
     * @return Presigned URL
     * @throws LakeFSObjectException if URL generation fails
     */
    @Override
    public String getPresignedObjectUrl(StorageConfig config, String repositoryName, String reference, String objectName) {
        validateObjectParams(repositoryName, reference, objectName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/refs/" + reference + "/objects?path=" + URLEncoder.encode(objectName, StandardCharsets.UTF_8) + "&presign=true";

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = response.getBody();
                return (String) responseBody.get("url");
            } catch (Exception e) {
                throw new LakeFSObjectException("Error generating presigned URL for: " + objectName, e);
            }
        });
    }

    /**
     * Deletes an object from LakeFS.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param objectName     Object name
     * @throws LakeFSObjectException if deletion fails
     */
    @Override
    public void deleteObject(StorageConfig config, String repositoryName, String branchName, String objectName) {
        validateObjectParams(repositoryName, branchName, objectName);
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/branches/" + branchName + "/objects/" + objectName;

                client.delete(url);
                log.info("Deleted object: {} from branch: {} in repository: {}", objectName, branchName, repositoryName);
            } catch (Exception e) {
                throw new LakeFSObjectException("Error deleting object: " + objectName, e);
            }
            return null;
        });
    }

    /**
     * Retrieves objects by metadata with AND/OR condition.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param reference      Branch name or commit ID
     * @param metadata       Metadata to filter
     * @param condition      Logical operator (AND/OR)
     * @return List of matching FileStorage objects
     * @throws LakeFSObjectException if retrieval fails
     */
    @Override
    public List<FileStorage> getObjectByMetadata(StorageConfig config, String repositoryName, String reference,
                                                 Map<String, String> metadata, IEnumLogicalOperator.Types condition) {
        validateRepositoryName(repositoryName);
        validateBranchName(reference);
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Metadata cannot be null or empty");
        }
        return executeWithRetry(() -> {
            try {
                List<FileStorage> allObjects = getObjects(config, repositoryName, reference, null);
                return allObjects.stream()
                        .filter(obj -> {
                            if (obj.metadata == null) return false;

                            boolean matches = condition == IEnumLogicalOperator.Types.AND
                                    ? metadata.entrySet().stream().allMatch(entry ->
                                    Objects.equals(obj.metadata.get(entry.getKey()), entry.getValue()))
                                    : metadata.entrySet().stream().anyMatch(entry ->
                                    Objects.equals(obj.metadata.get(entry.getKey()), entry.getValue()));

                            return matches;
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new LakeFSObjectException("Error retrieving objects by metadata", e);
            }
        });
    }

    /**
     * Lists all objects in a repository branch.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param reference      Branch name or commit ID
     * @param prefix         Object prefix filter (optional)
     * @return List of FileStorage objects
     * @throws LakeFSObjectException if listing fails
     */
    @Override
    public List<FileStorage> getObjects(StorageConfig config, String repositoryName, String reference, String prefix) {
        validateRepositoryName(repositoryName);
        validateBranchName(reference);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/refs/" + reference + "/objects";

                if (StringUtils.hasText(prefix)) {
                    url += "?prefix=" + prefix;
                }

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");

                List<FileStorage> fileStorageList = new ArrayList<>();
                for (Map<String, Object> item : results) {
                    FileStorage fileObject = new FileStorage();
                    fileObject.objectName = (String) item.get("path");
                    fileObject.size = item.get("size_bytes") != null ? ((Number) item.get("size_bytes")).longValue() : 0L;
                    fileObject.etag = (String) item.get("checksum");
                    fileObject.lastModified = item.get("mtime") != null ?
                            ZonedDateTime.parse((String) item.get("mtime")) : null;
                    fileObject.metadata = (Map<String, String>) item.get("metadata");
                    fileObject.pathType = (String) item.get("path_type");
                    fileStorageList.add(fileObject);
                }
                return fileStorageList;
            } catch (Exception e) {
                throw new LakeFSObjectException("Error listing objects in repository: " + repositoryName, e);
            }
        });
    }

    /**
     * Updates metadata for an object.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param objectName     Object name
     * @param metadata       New metadata
     * @throws LakeFSObjectException if metadata update fails
     */
    @Override
    public void updateMetadata(StorageConfig config, String repositoryName, String branchName, String objectName, Map<String, String> metadata) {
        validateObjectParams(repositoryName, branchName, objectName);
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata cannot be null");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = config.getUrl() + "/repositories/" + repositoryName + "/branches/" + branchName + "/objects/" + objectName + "/metadata";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> request = new HttpEntity<>(metadata, headers);

                client.put(url, request);
                log.info("Updated metadata for object: {} in branch: {} in repository: {}", objectName, branchName, repositoryName);
            } catch (Exception e) {
                throw new LakeFSObjectException("Error updating metadata for object: " + objectName, e);
            }
            return null;
        });
    }

    @FunctionalInterface
    private interface Supplier<T> {
        /**
         * Get t.
         *
         * @return the t
         * @throws LakeFSObjectException the lake fs object exception
         */
        T get() throws LakeFSObjectException;
    }
}