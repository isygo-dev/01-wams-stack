package eu.isygoit.storage.lfs.api.impl;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.lfs.api.ILakeFSApiService;
import eu.isygoit.storage.exception.LakeFSObjectException;
import eu.isygoit.storage.s3.api.IMinIOApiService;
import eu.isygoit.storage.s3.object.FileStorage;
import eu.isygoit.storage.lfs.config.LFSConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Enhanced service implementation for LakeFS data versioning operations with improved URL handling,
 * robust error handling, and optimized retry logic.
 */
@Slf4j
public abstract class LakeFSApiService implements ILakeFSApiService {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int DEFAULT_PRESIGNED_URL_EXPIRY_HOURS = 2;
    private static final String DEFAULT_BRANCH = "main";
    private static final String API_PATH_PREFIX = "/api/v1";
    private static final int CONNECTION_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

    private final Map<String, RestTemplate> lakeFSClientMap;
    private final IMinIOApiService minIOApiService;

    /**
     * Instantiates a new LakeFS API service.
     *
     * @param lakeFSClientMap  the LakeFS client map
     * @param minIOApiService  the MinIO API service
     */
    public LakeFSApiService(Map<String, RestTemplate> lakeFSClientMap, IMinIOApiService minIOApiService) {
        this.lakeFSClientMap = lakeFSClientMap;
        this.minIOApiService = minIOApiService;
    }

    /**
     * Builds a standardized LakeFS API URL.
     *
     * @param config       Storage configuration
     * @param pathSegments API path segments
     * @param queryParams  Query parameters
     * @return Constructed URL
     */
    private String buildLakeFSUrl(LFSConfig config, String[] pathSegments, Map<String, String> queryParams) {
        String baseUrl = config.getUrl().endsWith("/") ? config.getUrl() : config.getUrl() + "/";
        String apiPrefix = config.getApiPrefix() != null ? config.getApiPrefix() : API_PATH_PREFIX;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
        if (!baseUrl.contains(apiPrefix)) {
            builder.path(apiPrefix);
        }
        for (String segment : pathSegments) {
            builder.pathSegment(URLEncoder.encode(segment, StandardCharsets.UTF_8));
        }
        if (queryParams != null) {
            queryParams.forEach((key, value) -> builder.queryParam(key, URLEncoder.encode(value, StandardCharsets.UTF_8)));
        }
        String url = builder.build().toUriString();
        log.info("Constructed LakeFS URL: {}", url);
        return url;
    }

    /**
     * Configures a RestTemplate with timeouts and authentication.
     *
     * @param config Storage configuration
     * @return Configured RestTemplate
     */
    /*private RestTemplate configureRestTemplate(LFSConfig config) {
        RestTemplate restTemplate = new RestTemplate();
        // Configure timeouts
        restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
            setConnectTimeout(CONNECTION_TIMEOUT_MS);
            setReadTimeout(READ_TIMEOUT_MS);
        }});
        // Add custom message converter for text/plain
        restTemplate.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter() {
            @Override
            protected boolean canRead(MediaType mediaType) {
                return super.canRead(mediaType) || MediaType.TEXT_PLAIN.includes(mediaType);
            }
        });
        // Configure basic authentication
        restTemplate.getInterceptors().add((ClientHttpRequestInterceptor) (request, body, execution) -> {
            String auth = config.getUserName() + ":" + config.getPassword();
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            request.getHeaders().add("Authorization", "Basic " + new String(encodedAuth));
            return execution.execute(request, body);
        });
        return restTemplate;
    }*/
    private RestTemplate configureRestTemplate(LFSConfig config) {
        RestTemplate restTemplate = new RestTemplate();
        // Configure timeouts
        restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
            setConnectTimeout(CONNECTION_TIMEOUT_MS);
            setReadTimeout(READ_TIMEOUT_MS);
        }});
        // Remove custom text/plain converter to avoid JSON parsing for byte[] responses
        restTemplate.getMessageConverters().removeIf(converter ->
                converter instanceof MappingJackson2HttpMessageConverter);
        // Add default MappingJackson2HttpMessageConverter for JSON responses
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        // Configure basic authentication
        restTemplate.getInterceptors().add((ClientHttpRequestInterceptor) (request, body, execution) -> {
            String auth = config.getUserName() + ":" + config.getPassword();
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            request.getHeaders().add("Authorization", "Basic " + new String(encodedAuth));
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    /**
     * Retrieves or creates a LakeFS client connection for the specified tenant.
     *
     * @param config Storage configuration containing tenant, credentials, and endpoint
     * @return RestTemplate configured for LakeFS API calls
     * @throws IllegalArgumentException if config is invalid
     */
    @Override
    public RestTemplate getConnection(LFSConfig config) {
        validateConfig(config);
        return lakeFSClientMap.computeIfAbsent(config.getTenant(), k -> {
            try {
                return configureRestTemplate(config);
            } catch (Exception e) {
                log.error("Failed to create LakeFS client for tenant: {}", config.getTenant(), e);
                throw new LakeFSObjectException("Failed to initialize LakeFS client for tenant: " + config.getTenant(), e);
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
    public void updateConnection(LFSConfig config) {
        validateConfig(config);
        try {
            lakeFSClientMap.put(config.getTenant(), configureRestTemplate(config));
            log.info("Updated LakeFS connection for tenant: {}", config.getTenant());
        } catch (Exception e) {
            log.error("Failed to update LakeFS connection for tenant: {}", config.getTenant(), e);
            throw new LakeFSObjectException("Failed to update LakeFS connection for tenant: " + config.getTenant(), e);
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
    public boolean repositoryExists(LFSConfig config, String repositoryName) {
        validateRepositoryName(repositoryName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName}, null);
                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                return response.getStatusCode().is2xxSuccessful();
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return false;
                }
                throw new LakeFSObjectException("Error checking repository existence: " + repositoryName, e);
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
    public void createRepository(LFSConfig config, String repositoryName, String storageNamespace, String defaultBranch) {
        validateRepositoryName(repositoryName);
        if (!StringUtils.hasText(storageNamespace)) {
            throw new IllegalArgumentException("Storage namespace cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                if (!minIOApiService.bucketExists(config.getS3Config(), storageNamespace)) {
                    minIOApiService.makeBucket(config.getS3Config(), storageNamespace);
                }
                if (!repositoryExists(config, repositoryName)) {
                    RestTemplate client = getConnection(config);
                    String url = buildLakeFSUrl(config, new String[]{"repositories"}, null);

                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("name", repositoryName);
                    requestBody.put("storage_namespace", "s3://" + storageNamespace);
                    requestBody.put("default_branch", StringUtils.hasText(defaultBranch) ? defaultBranch : DEFAULT_BRANCH);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                    client.postForEntity(url, request, Map.class);
                    log.info("Created repository: {} with namespace: {}", repositoryName, storageNamespace);
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
    public void deleteObjects(LFSConfig config, String repositoryName, String branchName, List<String> objectNames) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        if (objectNames == null || objectNames.isEmpty()) {
            throw new IllegalArgumentException("Object names list cannot be null or empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches", branchName, "objects", "delete"}, null);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("paths", objectNames);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Deleted {} objects from branch: {} in repository: {}", objectNames.size(), branchName, repositoryName);
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error deleting objects from repository: " + repositoryName + ", branch: " + branchName + ", HTTP status: " + e.getStatusCode(), e);
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
    public String commit(LFSConfig config, String repositoryName, String branchName, String message, Map<String, String> metadata) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Commit message cannot be empty");
        }
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches", branchName, "commits"}, null);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("message", message);
                if (metadata != null && !metadata.isEmpty()) {
                    requestBody.put("metadata", metadata);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map> response = client.postForEntity(url, request, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSObjectException("Empty response body for commit"));
                String commitId = (String) responseBody.get("id");
                if (!StringUtils.hasText(commitId)) {
                    throw new LakeFSObjectException("Commit ID not found in response");
                }
                log.info("Committed changes to branch: {} in repository: {}, commit ID: {}", branchName, repositoryName, commitId);
                return commitId;
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error committing to branch: " + branchName + ", HTTP status: " + e.getStatusCode(), e);
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
    public String merge(LFSConfig config, String repositoryName, String sourceBranchName, String destinationBranchName, String message) {
        validateRepositoryName(repositoryName);
        validateBranchName(sourceBranchName);
        validateBranchName(destinationBranchName);
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Merge message cannot be empty");
        }
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "refs", destinationBranchName, "merge", sourceBranchName}, null);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("message", message);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map> response = client.postForEntity(url, request, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSObjectException("Empty response body for merge"));
                String mergeCommitId = (String) responseBody.get("reference");
                if (!StringUtils.hasText(mergeCommitId)) {
                    throw new LakeFSObjectException("Merge commit ID not found in response");
                }
                log.info("Merged branch: {} into: {} in repository: {}, merge commit: {}", sourceBranchName, destinationBranchName, repositoryName, mergeCommitId);
                return mergeCommitId;
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error merging branch: " + sourceBranchName + " into: " + destinationBranchName + ", HTTP status: " + e.getStatusCode(), e);
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
    public List<String> getBranches(LFSConfig config, String repositoryName) {
        validateRepositoryName(repositoryName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches"}, null);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSObjectException("Empty response body for branches"));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                return Optional.ofNullable(results)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(branch -> (String) branch.get("id"))
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error listing branches in repository: " + repositoryName + ", HTTP status: " + e.getStatusCode(), e);
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
    public List<String> getRepositories(LFSConfig config) {
        validateConfig(config);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories"}, null);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSObjectException("Empty response body for repositories"));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                List<String> repositories = Optional.ofNullable(results)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(repo -> (String) repo.get("id"))
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
                log.info("Retrieved {} repositories for tenant: {}", repositories.size(), config.getTenant());
                return repositories;
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error listing repositories for tenant: " + config.getTenant() + ", HTTP status: " + e.getStatusCode(), e);
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
    public List<Map<String, Object>> getCommitHistory(LFSConfig config, String repositoryName, String branchName, int limit) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                Map<String, String> queryParams = limit > 0 ? Map.of("amount", String.valueOf(limit)) : null;
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "refs", branchName, "commits"}, queryParams);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSObjectException("Empty response body for commit history"));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                return Optional.ofNullable(results).orElse(Collections.emptyList());
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error retrieving commit history for branch: " + branchName + ", HTTP status: " + e.getStatusCode(), e);
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
    public List<Map<String, Object>> getDiff(LFSConfig config, String repositoryName, String leftRef, String rightRef) {
        validateRepositoryName(repositoryName);
        validateBranchName(leftRef);
        validateBranchName(rightRef);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "refs", leftRef, "diff", rightRef}, null);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSObjectException("Empty response body for diff"));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                return Optional.ofNullable(results).orElse(Collections.emptyList());
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error retrieving diff between: " + leftRef + " and: " + rightRef + ", HTTP status: " + e.getStatusCode(), e);
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
    public void revert(LFSConfig config, String repositoryName, String branchName, String commitId) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        if (!StringUtils.hasText(commitId)) {
            throw new IllegalArgumentException("Commit ID cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches", branchName, "revert"}, null);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("ref", commitId);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Reverted branch: {} to commit: {} in repository: {}", branchName, commitId, repositoryName);
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error reverting branch: " + branchName + " to commit: " + commitId + ", HTTP status: " + e.getStatusCode(), e);
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
    public void deleteBranch(LFSConfig config, String repositoryName, String branchName) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches", branchName}, null);

                client.delete(url);
                log.info("Deleted branch: {} from repository: {}", branchName, repositoryName);
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error deleting branch: " + branchName + ", HTTP status: " + e.getStatusCode(), e);
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
    public void deleteRepository(LFSConfig config, String repositoryName) {
        validateRepositoryName(repositoryName);
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName}, null);

                client.delete(url);
                log.info("Deleted repository: {}", repositoryName);
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error deleting repository: " + repositoryName + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    /**
     * Executes an operation with retry logic and exponential backoff.
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
                    Thread.sleep((long) RETRY_DELAY_MS * (1 << attempt)); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LakeFSObjectException("Retry interrupted", ie);
                }
                log.warn("Retrying operation, attempt {}/{}", attempt + 1, MAX_RETRIES);
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
    private void validateConfig(LFSConfig config) {
        if (config == null ||
                !StringUtils.hasText(config.getTenant()) ||
                !StringUtils.hasText(config.getUrl()) ||
                !StringUtils.hasText(config.getUserName()) ||
                !StringUtils.hasText(config.getPassword())) {
            throw new IllegalArgumentException("Invalid storage configuration: tenant, URL, username, and password must be provided");
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
        if (repositoryName.contains("/")) {
            throw new IllegalArgumentException("Repository name cannot contain slashes");
        }
    }

    /**
     * Validates branch name or reference.
     *
     * @param branchName Name of the branch or reference
     * @throws IllegalArgumentException if invalid
     */
    private void validateBranchName(String branchName) {
        if (!StringUtils.hasText(branchName)) {
            throw new IllegalArgumentException("Branch name or reference cannot be empty");
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
    public boolean branchExists(LFSConfig config, String repositoryName, String branchName) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches", branchName}, null);
                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                return response.getStatusCode().is2xxSuccessful();
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return false;
                }
                throw new LakeFSObjectException("Error checking branch existence: " + branchName, e);
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
    public void createBranch(LFSConfig config, String repositoryName, String branchName, String sourceBranch) {
        validateRepositoryName(repositoryName);
        validateBranchName(branchName);
        validateBranchName(sourceBranch);
        executeWithRetry(() -> {
            try {
                if (!branchExists(config, repositoryName, branchName)) {
                    RestTemplate client = getConnection(config);
                    String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches"}, null);
                    log.info("Attempting to create branch: {} from source: {} in repository: {}, URL: {}", branchName, sourceBranch, repositoryName, url);

                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("name", branchName);
                    requestBody.put("source", sourceBranch);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                    ResponseEntity<String> response = client.postForEntity(url, request, String.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("Created branch: {} from source: {} in repository: {}", branchName, sourceBranch, repositoryName);
                    } else {
                        log.error("Failed to create branch: {} in repository: {}, HTTP status: {}, Response: {}", branchName, repositoryName, response.getStatusCode(), response.getBody());
                        throw new LakeFSObjectException("Failed to create branch: " + branchName + ", HTTP status: " + response.getStatusCode() + ", Response: " + response.getBody());
                    }
                }
            } catch (HttpClientErrorException e) {
                log.error("Error creating branch: {} in repository: {}, HTTP status: {}, Response: {}", branchName, repositoryName, e.getStatusCode(), e.getResponseBodyAsString());
                throw new LakeFSObjectException("Error creating branch: " + branchName + ", HTTP status: " + e.getStatusCode() + ", Response: " + e.getResponseBodyAsString(), e);
            } catch (Exception e) {
                log.error("Unexpected error creating branch: {} in repository: {}", branchName, repositoryName, e);
                throw new LakeFSObjectException("Unexpected error creating branch: " + branchName, e);
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
    public void uploadFile(LFSConfig config, String repositoryName, String branchName, String path, String objectName,
                           MultipartFile multipartFile, Map<String, String> metadata) {
        validateUploadParams(repositoryName, branchName, objectName, multipartFile);
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String fullPath = StringUtils.hasText(path) ? path + "/" + objectName : objectName;
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches", branchName, "objects"},
                        Map.of("path", URLEncoder.encode(fullPath, StandardCharsets.UTF_8)));

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                // Use ByteArrayResource instead of InputStreamResource
                body.add("content", new ByteArrayResource(multipartFile.getBytes()) {
                    @Override
                    public String getFilename() {
                        return objectName;
                    }
                });

                if (metadata != null && !metadata.isEmpty()) {
                    metadata.forEach(body::add);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Uploaded file: {} to branch: {} in repository: {}", fullPath, branchName, repositoryName);
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error uploading file: " + objectName + ", HTTP status: " + e.getStatusCode(), e);
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
    public byte[] getObject(LFSConfig config, String repositoryName, String reference, String objectName) {
        validateObjectParams(repositoryName, reference, objectName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "refs", reference, "objects"},
                        Map.of("path", URLEncoder.encode(objectName, StandardCharsets.UTF_8)));

                // Use exchange to explicitly handle the response as a byte array
                ResponseEntity<byte[]> response = client.exchange(url, HttpMethod.GET, null, byte[].class);
                return Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSObjectException("Empty response body for object: " + objectName));
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error retrieving object: " + objectName + ", HTTP status: " + e.getStatusCode(), e);
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
    public String getPresignedObjectUrl(LFSConfig config, String repositoryName, String reference, String objectName) {
        validateObjectParams(repositoryName, reference, objectName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "refs", reference, "objects"},
                        Map.of("path", URLEncoder.encode(objectName, StandardCharsets.UTF_8), "presign", "true"));

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSObjectException("Empty response body when getting presigned URL for: " + objectName));
                String presignedUrl = (String) responseBody.get("url");
                if (!StringUtils.hasText(presignedUrl)) {
                    throw new LakeFSObjectException("No presigned URL returned for object: " + objectName);
                }
                return presignedUrl;
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error generating presigned URL for: " + objectName + ", HTTP status: " + e.getStatusCode(), e);
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
    public void deleteObject(LFSConfig config, String repositoryName, String branchName, String objectName) {
        validateObjectParams(repositoryName, branchName, objectName);
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                // Use query parameter 'path' instead of appending objectName to the path
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches", branchName, "objects"},
                        Map.of("path", URLEncoder.encode(objectName, StandardCharsets.UTF_8)));

                client.delete(url);
                log.info("Deleted object: {} from branch: {} in repository: {}", objectName, branchName, repositoryName);
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error deleting object: " + objectName + ", HTTP status: " + e.getStatusCode(), e);
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
    public List<FileStorage> getObjectByMetadata(LFSConfig config, String repositoryName, String reference,
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
                            return condition == IEnumLogicalOperator.Types.AND
                                    ? metadata.entrySet().stream().allMatch(entry ->
                                    Objects.equals(obj.metadata.get(entry.getKey()), entry.getValue()))
                                    : metadata.entrySet().stream().anyMatch(entry ->
                                    Objects.equals(obj.metadata.get(entry.getKey()), entry.getValue()));
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new LakeFSObjectException("Error retrieving objects by metadata in repository: " + repositoryName, e);
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
    public List<FileStorage> getObjects(LFSConfig config, String repositoryName, String reference, String prefix) {
        validateRepositoryName(repositoryName);
        validateBranchName(reference);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                // Use an empty path if prefix is null to list all objects
                Map<String, String> queryParams = StringUtils.hasText(prefix) ? Map.of("prefix", prefix) : Map.of("path", "");
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "refs", reference, "objects"}, queryParams);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSObjectException("Empty response body for object listing"));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                List<FileStorage> fileStorageList = new ArrayList<>();
                for (Map<String, Object> item : Optional.ofNullable(results).orElse(Collections.emptyList())) {
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
                log.info("Retrieved {} objects from repository: {}, branch: {}", fileStorageList.size(), repositoryName, reference);
                return fileStorageList;
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error listing objects in repository: " + repositoryName + ", HTTP status: " + e.getStatusCode(), e);
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
    public void updateMetadata(LFSConfig config, String repositoryName, String branchName, String objectName, Map<String, String> metadata) {
        validateObjectParams(repositoryName, branchName, objectName);
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata cannot be null");
        }
        executeWithRetry(() -> {
            try {
                // Step 1: Retrieve the existing object content
                byte[] objectContent = getObject(config, repositoryName, branchName, objectName);
                if (objectContent == null) {
                    throw new LakeFSObjectException("Object not found: " + objectName);
                }

                // Step 2: Re-upload the object with new metadata
                RestTemplate client = getConnection(config);
                String fullPath = objectName; // Assuming objectName is the full path; adjust if a path prefix is needed
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches", branchName, "objects"},
                        Map.of("path", URLEncoder.encode(fullPath, StandardCharsets.UTF_8)));

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("content", new ByteArrayResource(objectContent) {
                    @Override
                    public String getFilename() {
                        return objectName;
                    }
                });

                if (metadata != null && !metadata.isEmpty()) {
                    metadata.forEach(body::add);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Updated metadata for object: {} in branch: {} in repository: {}", objectName, branchName, repositoryName);
            } catch (HttpClientErrorException e) {
                throw new LakeFSObjectException("Error updating metadata for object: " + objectName + ", HTTP status: " + e.getStatusCode(), e);
            } catch (Exception e) {
                throw new LakeFSObjectException("Error updating metadata for object: " + objectName, e);
            }
            return null;
        });
    }
    @FunctionalInterface
    private interface Supplier<T> {
        T get() throws LakeFSObjectException;
    }
}