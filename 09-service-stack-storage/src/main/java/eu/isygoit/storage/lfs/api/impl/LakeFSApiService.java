package eu.isygoit.storage.lfs.api.impl;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.exception.LakeFSException;
import eu.isygoit.storage.lfs.api.ILakeFSApiService;
import eu.isygoit.storage.lfs.config.LFSConfig;
import eu.isygoit.storage.s3.api.IMinIOApiService;
import eu.isygoit.storage.s3.object.FileStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
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
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Lake fs api service.
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
    private static final int DEFAULT_PAGINATION_LIMIT = 100;

    private final Map<String, RestTemplate> lakeFSClientMap;
    private final IMinIOApiService minIOApiService;

    /**
     * Instantiates a new Lake fs api service.
     *
     * @param lakeFSClientMap the lake fs client map
     * @param minIOApiService the min io api service
     */
    public LakeFSApiService(Map<String, RestTemplate> lakeFSClientMap, IMinIOApiService minIOApiService) {
        this.lakeFSClientMap = lakeFSClientMap;
        this.minIOApiService = minIOApiService;
    }

    private static void validateStorageNamespace(String storageNamespace) {
        if (!StringUtils.hasText(storageNamespace)) {
            throw new IllegalArgumentException("Storage namespace cannot be empty");
        }
    }

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

    public void setupLakeFS(LFSConfig config, String username, String accessKey, String secretKey) {
        log.info("Initializing LakeFS at URL: {}", config.getUrl());

        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);

                Map<String, Object> key = Map.of(
                        "access_key_id", accessKey,
                        "secret_access_key", secretKey
                );

                Map<String, Object> setupRequest = Map.of(
                        "username", username,
                        "key", key
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(setupRequest, headers);

                String url = buildLakeFSUrl(config, new String[]{"setup_lakefs"}, null);
                ResponseEntity<String> response = client.postForEntity(url, request, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("LakeFS setup completed successfully: {}", response.getBody());
                } else {
                    throw new LakeFSException("LakeFS initialization failed: " + response.getBody());
                }

            } catch (HttpClientErrorException e) {
                throw new LakeFSException("LakeFS setup failed with HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    @Override
    public RestTemplate getConnection(LFSConfig config) {
        validateConfig(config);
        return lakeFSClientMap.computeIfAbsent(config.getTenant(), k -> {
            try {
                return configureRestTemplate(config);
            } catch (Exception e) {
                log.error("Failed to create LakeFS client for tenant: {}", config.getTenant(), e);
                throw new LakeFSException("Failed to initialize LakeFS client for tenant: " + config.getTenant(), e);
            }
        });
    }

    @Override
    public void updateConnection(LFSConfig config) {
        validateConfig(config);
        try {
            lakeFSClientMap.put(config.getTenant(), configureRestTemplate(config));
            log.info("Updated LakeFS connection for tenant: {}", config.getTenant());
        } catch (Exception e) {
            log.error("Failed to update LakeFS connection for tenant: {}", config.getTenant(), e);
            throw new LakeFSException("Failed to update LakeFS connection for tenant: " + config.getTenant(), e);
        }
    }

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
                throw new LakeFSException("Error checking repository existence: " + repositoryName, e);
            }
        });
    }

    @Override
    public void createRepository(LFSConfig config, String repositoryName, String storageNamespace, String defaultBranch) {
        validateRepositoryName(repositoryName);
        validateStorageNamespace(storageNamespace);
        executeWithRetry(() -> {
            try {
                if (config.getS3Config() != null) {
                    // Use S3 storage
                    if (!minIOApiService.bucketExists(config.getS3Config(), storageNamespace)) {
                        minIOApiService.makeBucket(config.getS3Config(), storageNamespace);
                    }
                }

                if (!repositoryExists(config, repositoryName)) {
                    RestTemplate client = getConnection(config);
                    String url = buildLakeFSUrl(config, new String[]{"repositories"}, null);

                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("name", repositoryName);
                    requestBody.put("storage_namespace", config.getS3Config() != null? "s3://" + storageNamespace: "local://" + storageNamespace);
                    requestBody.put("default_branch", StringUtils.hasText(defaultBranch) ? defaultBranch : DEFAULT_BRANCH);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                    client.postForEntity(url, request, Map.class);
                    log.info("Created repository: {} with namespace: {}", repositoryName, config.getS3Config() != null? "s3://" + storageNamespace: "local://" + storageNamespace);
                }
            } catch (Exception e) {
                throw new LakeFSException("Error creating repository: " + repositoryName, e);
            }
            return null;
        });
    }

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
                throw new LakeFSException("Error deleting objects from repository: " + repositoryName + ", branch: " + branchName + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

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
                        .orElseThrow(() -> new LakeFSException("Empty response body for commit"));
                String commitId = (String) responseBody.get("id");
                if (!StringUtils.hasText(commitId)) {
                    throw new LakeFSException("Commit ID not found in response");
                }
                log.info("Committed changes to branch: {} in repository: {}, commit ID: {}", branchName, repositoryName, commitId);
                return commitId;
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error committing to branch: " + branchName + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

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
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "refs", sourceBranchName , "merge", destinationBranchName}, null);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("message", message);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map> response = client.postForEntity(url, request, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for merge"));
                String mergeCommitId = (String) responseBody.get("reference");
                if (!StringUtils.hasText(mergeCommitId)) {
                    throw new LakeFSException("Merge commit ID not found in response");
                }
                log.info("Merged branch: {} into: {} in repository: {}, merge commit: {}", sourceBranchName, destinationBranchName, repositoryName, mergeCommitId);
                return mergeCommitId;
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error merging branch: " + sourceBranchName + " into: " + destinationBranchName + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public List<String> getBranches(LFSConfig config, String repositoryName) {
        validateRepositoryName(repositoryName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches"}, null);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for branches"));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                return Optional.ofNullable(results)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(branch -> (String) branch.get("id"))
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error listing branches in repository: " + repositoryName + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public List<String> getRepositories(LFSConfig config) {
        validateConfig(config);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"repositories"}, null);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for repositories"));
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
                throw new LakeFSException("Error listing repositories for tenant: " + config.getTenant() + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

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
                        .orElseThrow(() -> new LakeFSException("Empty response body for commit history"));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                return Optional.ofNullable(results).orElse(Collections.emptyList());
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error retrieving commit history for branch: " + branchName + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public List<Map<String, Object>> getDiff(LFSConfig config, String repositoryName, String leftRef, String rightRef) {
        validateRepositoryName(repositoryName);
        validateBranchName(leftRef);
        validateBranchName(rightRef);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config,
                        new String[]{"repositories", repositoryName, "refs", leftRef, "diff", rightRef},
                        Map.of("amount", "1000")); // Ensure we get all diffs

                log.debug("Fetching diff from URL: {}", url);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for diff"));

                log.debug("Diff response: {}", responseBody);

                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                return Optional.ofNullable(results).orElse(Collections.emptyList());
            } catch (HttpClientErrorException e) {
                log.error("Diff API error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
                throw new LakeFSException("Error retrieving diff between: " + leftRef + " and: " + rightRef + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

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
                requestBody.put("parent_number", 1); // Default to 1 for non-merge commits

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Reverted branch: {} to commit: {} in repository: {}", branchName, commitId, repositoryName);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error reverting branch: " + branchName + " to commit: " + commitId + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

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
                throw new LakeFSException("Error deleting branch: " + branchName + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    @Override
    public void deleteRepository(LFSConfig config, String repositoryName, boolean force) {
        validateRepositoryName(repositoryName);
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                Map<String, String> queryParams = Map.of( "force", String.valueOf(Boolean.TRUE));
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName}, queryParams);
                client.delete(url);
                log.info("Deleted repository: {}", repositoryName);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error deleting repository: " + repositoryName + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    private <T> T executeWithRetry(Supplier<T> operation) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (LakeFSException e) {
                attempt++;
                if (attempt == MAX_RETRIES) {
                    throw e;
                }
                try {
                    Thread.sleep((long) RETRY_DELAY_MS * (1 << attempt)); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LakeFSException("Retry interrupted", ie);
                }
                log.warn("Retrying operation, attempt {}/{}", attempt + 1, MAX_RETRIES);
            }
        }
        throw new LakeFSException("Operation failed after maximum retries");
    }

    private void validateConfig(LFSConfig config) {
        if (config == null ||
                !StringUtils.hasText(config.getTenant()) ||
                !StringUtils.hasText(config.getUrl()) ||
                !StringUtils.hasText(config.getUserName()) ||
                !StringUtils.hasText(config.getPassword())) {
            throw new IllegalArgumentException("Invalid storage configuration: tenant, URL, username, and password must be provided");
        }
    }

    private void validateRepositoryName(String repositoryName) {
        if (!StringUtils.hasText(repositoryName)) {
            throw new IllegalArgumentException("Repository name cannot be empty");
        }
        if (repositoryName.contains("/")) {
            throw new IllegalArgumentException("Repository name cannot contain slashes");
        }
    }

    private void validateBranchName(String branchName) {
        if (!StringUtils.hasText(branchName)) {
            throw new IllegalArgumentException("Branch name or reference cannot be empty");
        }
    }

    private void validateObjectParams(String repositoryName, String reference, String objectName) {
        validateRepositoryName(repositoryName);
        validateBranchName(reference);
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("Object name cannot be empty");
        }
    }

    private void validateUploadParams(String repositoryName, String branchName, String objectName, MultipartFile file) {
        validateObjectParams(repositoryName, branchName, objectName);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Multipart file cannot be null or empty");
        }
    }

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
                throw new LakeFSException("Error checking branch existence: " + branchName, e);
            }
        });
    }

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
                        throw new LakeFSException("Failed to create branch: " + branchName + ", HTTP status: " + response.getStatusCode() + ", Response: " + response.getBody());
                    }
                }
            } catch (HttpClientErrorException e) {
                log.error("Error creating branch: {} in repository: {}, HTTP status: {}, Response: {}", branchName, repositoryName, e.getStatusCode(), e.getResponseBodyAsString());
                throw new LakeFSException("Error creating branch: " + branchName + ", HTTP status: " + e.getStatusCode() + ", Response: " + e.getResponseBodyAsString(), e);
            } catch (Exception e) {
                log.error("Unexpected error creating branch: {} in repository: {}", branchName, repositoryName, e);
                throw new LakeFSException("Unexpected error creating branch: " + branchName, e);
            }
            return null;
        });
    }

    @Override
    public void uploadFile(LFSConfig config, String repositoryName, String branchName, String path, String objectName,
                           MultipartFile file) {
        validateUploadParams(repositoryName, branchName, objectName, file);
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String fullPath = StringUtils.hasText(path) ? path + "/" + objectName : objectName;
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches", branchName, "objects"},
                        Map.of("path", URLEncoder.encode(fullPath, StandardCharsets.UTF_8)));

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                // Use ByteArrayResource instead of InputStreamResource
                body.add("content", new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return objectName;
                    }
                });

                /*
                if (metadata != null && !metadata.isEmpty()) {
                    metadata.forEach(body::add);
                }
                 */

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Uploaded file: {} to branch: {} in repository: {}", fullPath, branchName, repositoryName);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error uploading file: " + objectName + ", HTTP status: " + e.getStatusCode(), e);
            } catch (Exception e) {
                throw new LakeFSException("Error uploading file: " + objectName, e);
            }
            return null;
        });
    }

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
                        .orElseThrow(() -> new LakeFSException("Empty response body for object: " + objectName));
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error retrieving object: " + objectName + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public String getPresignedObjectUrl(LFSConfig config, String repositoryName, String reference, String objectName, int expiryHours) {
        validateObjectParams(repositoryName, reference, objectName);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("path", URLEncoder.encode(objectName, StandardCharsets.UTF_8));
                queryParams.put("presign", "true");
                if (expiryHours > 0) {
                    queryParams.put("expiry", String.valueOf(expiryHours * 3600));
                }
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "refs", reference, "objects"}, queryParams);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for presigned URL"));
                String presignedUrl = (String) responseBody.get("url");
                if (!StringUtils.hasText(presignedUrl)) {
                    throw new LakeFSException("No presigned URL returned for object: " + objectName);
                }
                return presignedUrl;
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error generating presigned URL for: " + objectName, e);
            }
        });
    }

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
                throw new LakeFSException("Error deleting object: " + objectName + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    @Override
    public List<FileStorage> getObjectByMetadata(LFSConfig config, String repositoryName, String reference,
                                                 Map<String, String> metadata, IEnumLogicalOperator.Types condition) {
        validateRepositoryName(repositoryName);
        validateBranchName(reference);
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Metadata cannot be null or empty");
        }
        if (condition == null) {
            throw new IllegalArgumentException("Logical operator condition cannot be null");
        }

        return executeWithRetry(() -> {
            try {
                // Get all objects from the repository/branch
                List<FileStorage> allObjects = getObjects(config, repositoryName, reference, "");

                if (allObjects.isEmpty()) {
                    log.debug("No objects found in repository: {}, branch: {}", repositoryName, reference);
                    return Collections.emptyList();
                }

                // Filter objects based on metadata and condition
                List<FileStorage> filteredObjects = allObjects.stream()
                        .filter(obj -> obj.metadata != null && !obj.metadata.isEmpty())
                        .filter(obj -> {
                            boolean matches = false;

                            if (condition == IEnumLogicalOperator.Types.AND) {
                                // For AND: all provided metadata entries must match
                                matches = metadata.entrySet().stream()
                                        .allMatch(entry -> {
                                            String objectValue = obj.metadata.get(entry.getKey());
                                            return objectValue != null && objectValue.equals(entry.getValue());
                                        });
                            } else if (condition == IEnumLogicalOperator.Types.OR) {
                                // For OR: at least one provided metadata entry must match
                                matches = metadata.entrySet().stream()
                                        .anyMatch(entry -> {
                                            String objectValue = obj.metadata.get(entry.getKey());
                                            return objectValue != null && objectValue.equals(entry.getValue());
                                        });
                            }

                            return matches;
                        })
                        .collect(Collectors.toList());

                log.info("Found {} objects matching metadata criteria (condition: {}) in repository: {}, branch: {}",
                        filteredObjects.size(), condition, repositoryName, reference);

                return filteredObjects;

            } catch (Exception e) {
                log.error("Error retrieving objects by metadata in repository: {}, branch: {}", repositoryName, reference, e);
                throw new LakeFSException("Error retrieving objects by metadata in repository: " + repositoryName + ", branch: " + reference, e);
            }
        });
    }

    @Override
    public List<FileStorage> getObjects(LFSConfig config, String repositoryName, String reference, String prefix) {
        validateRepositoryName(repositoryName);
        validateBranchName(reference);
        // Convert null prefix to empty string
        final String effectivePrefix = prefix != null ? prefix : "";
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                Map<String, String> queryParams = Map.of(
                        "prefix", effectivePrefix
                );

                String url = buildLakeFSUrl(config,
                        new String[]{"repositories", repositoryName, "refs", reference, "objects", "ls"},
                        queryParams);

                log.debug("Listing objects from URL: {}", url);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for object listing"));

                log.debug("Objects response: {}", responseBody);

                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                List<FileStorage> fileStorageList = new ArrayList<>();
                for (Map<String, Object> item : Optional.ofNullable(results).orElse(Collections.emptyList())) {
                    FileStorage fileObject = new FileStorage();
                    fileObject.objectName = (String) item.get("path");
                    fileObject.size = item.get("size_bytes") != null ? ((Number) item.get("size_bytes")).longValue() : 0L;
                    fileObject.etag = (String) item.get("checksum");
                    fileObject.lastModified = item.get("mtime") != null ?
                            Instant.ofEpochSecond((Integer) item.get("mtime")).atZone(ZoneId.systemDefault()): null;
                    fileObject.metadata = (Map<String, String>) item.get("metadata");
                    fileObject.pathType = (String) item.get("path_type");
                    fileStorageList.add(fileObject);
                }
                log.info("Retrieved {} objects from repository: {}, branch: {}", fileStorageList.size(), repositoryName, reference);
                return fileStorageList;
            } catch (HttpClientErrorException e) {
                log.error("Objects listing error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
                throw new LakeFSException("Error listing objects in repository: " + repositoryName +
                        ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

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
                    throw new LakeFSException("Object not found: " + objectName);
                }

                // Step 2: Re-upload the object with new metadata
                RestTemplate client = getConnection(config);
                String fullPath = objectName; // Assuming objectName is the full path; adjust if a path prefix is needed
                String url = buildLakeFSUrl(config, new String[]{"repositories", repositoryName, "branches", branchName, "objects", "stat", "user_metadata"},
                        Map.of("path", URLEncoder.encode(fullPath, StandardCharsets.UTF_8)));

                Map<String, Map<String, String>> body = Map.of("set", metadata);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Map<String, String>>> request = new HttpEntity<>(body, headers);

                client.put(url, request, Map.class);
                log.info("Updated metadata for object: {} in branch: {} in repository: {}", objectName, branchName, repositoryName);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error updating metadata for object: " + objectName + ", HTTP status: " + e.getStatusCode(), e);
            } catch (Exception e) {
                throw new LakeFSException("Error updating metadata for object: " + objectName, e);
            }
            return null;
        });
    }

    @Override
    public Map<String, Object> getPolicy(LFSConfig config, String policyId) {
        if (!StringUtils.hasText(policyId)) {
            throw new IllegalArgumentException("Policy ID cannot be empty");
        }
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "policies", policyId}, null);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for policy: " + policyId));
                log.info("Retrieved policy: {}", policyId);
                return responseBody;
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error retrieving policy: " + policyId + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public Map<String, Object> getUser(LFSConfig config, String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "users", userId}, null);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for user: " + userId));
                log.info("Retrieved user: {}", userId);
                return responseBody;
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error retrieving user: " + userId + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public List<String> listGroupMembers(LFSConfig config, String groupId, String after, int amount) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("Group ID cannot be empty");
        }
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("amount", String.valueOf(amount > 0 ? amount : DEFAULT_PAGINATION_LIMIT));
                if (StringUtils.hasText(after)) {
                    queryParams.put("after", after);
                }
                String url = buildLakeFSUrl(config, new String[]{"auth", "groups", groupId, "members"}, queryParams);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for group members: " + groupId));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                return Optional.ofNullable(results)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(member -> (String) member.get("id"))
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error listing group members for group: " + groupId + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public List<String> listGroupPolicies(LFSConfig config, String groupId, String after, int amount) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("Group ID cannot be empty");
        }
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("amount", String.valueOf(amount > 0 ? amount : DEFAULT_PAGINATION_LIMIT));
                if (StringUtils.hasText(after)) {
                    queryParams.put("after", after);
                }
                String url = buildLakeFSUrl(config, new String[]{"auth", "groups", groupId, "policies"}, queryParams);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for group policies: " + groupId));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                return Optional.ofNullable(results)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(policy -> (String) policy.get("id"))
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error listing policies for group: " + groupId + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public List<String> listGroups(LFSConfig config, String after, int amount) {
        validateConfig(config);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("amount", String.valueOf(amount > 0 ? amount : DEFAULT_PAGINATION_LIMIT));
                if (StringUtils.hasText(after)) {
                    queryParams.put("after", after);
                }
                String url = buildLakeFSUrl(config, new String[]{"auth", "groups"}, queryParams);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for groups"));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                List<String> groups = Optional.ofNullable(results)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(group -> (String) group.get("id"))
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
                log.info("Retrieved {} groups for tenant: {}", groups.size(), config.getTenant());
                return groups;
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error listing groups for tenant: " + config.getTenant() + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public List<String> listPolicies(LFSConfig config, String after, int amount) {
        validateConfig(config);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("amount", String.valueOf(amount > 0 ? amount : DEFAULT_PAGINATION_LIMIT));
                if (StringUtils.hasText(after)) {
                    queryParams.put("after", after);
                }
                String url = buildLakeFSUrl(config, new String[]{"auth", "policies"}, queryParams);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for policies"));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                List<String> policies = Optional.ofNullable(results)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(policy -> (String) policy.get("id"))
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
                log.info("Retrieved {} policies for tenant: {}", policies.size(), config.getTenant());
                return policies;
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error listing policies for tenant: " + config.getTenant() + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public List<String> listUsers(LFSConfig config, String after, int amount) {
        validateConfig(config);
        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("amount", String.valueOf(amount > 0 ? amount : DEFAULT_PAGINATION_LIMIT));
                if (StringUtils.hasText(after)) {
                    queryParams.put("after", after);
                }
                String url = buildLakeFSUrl(config, new String[]{"auth", "users"}, queryParams);

                ResponseEntity<Map> response = client.getForEntity(url, Map.class);
                Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                        .orElseThrow(() -> new LakeFSException("Empty response body for users"));
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                List<String> users = Optional.ofNullable(results)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(user -> (String) user.get("id"))
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
                log.info("Retrieved {} users for tenant: {}", users.size(), config.getTenant());
                return users;
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error listing users for tenant: " + config.getTenant() + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public void createPolicy(LFSConfig config, String policyId, List<Map<String, Object>> statement) {
        if (!StringUtils.hasText(policyId)) {
            throw new IllegalArgumentException("Policy ID cannot be empty");
        }
        if (statement == null || statement.isEmpty()) {
            throw new IllegalArgumentException("Policy statement cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "policies"}, null);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("id", policyId);
                requestBody.put("statement", statement);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Created policy: {}", policyId);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error creating policy: " + policyId + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    public boolean isUserExists(LFSConfig config, String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }

        return executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "users", userId}, null);
                client.getForEntity(url, Map.class);
                log.info("User '{}' exists.", userId);
                return true;
            } catch (HttpClientErrorException.NotFound e) {
                log.info("User '{}' does not exist.", userId);
                return false;
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error checking user existence: " + userId + ", HTTP status: " + e.getStatusCode(), e);
            }
        });
    }

    @Override
    public void createUser(LFSConfig config, String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }

        executeWithRetry(() -> {
            if (isUserExists(config, userId)) {
                log.info("User '{}' already exists. Skipping creation.", userId);
                return null;
            }

            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "users"}, null);

                Map<String, Object> requestBody = Map.of("id", userId, "invite_user", String.valueOf(Boolean.TRUE));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Created user: {}", userId);

            } catch (HttpClientErrorException.Conflict conflict) {
                log.warn("User '{}' already exists (409 Conflict).", userId);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error creating user: " + userId + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }



    @Override
    public void createGroup(LFSConfig config, String groupId) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("Group ID cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "groups"}, null);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("id", groupId);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Created group: {}", groupId);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error creating group: " + groupId + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    @Override
    public void deletePolicy(LFSConfig config, String policyId) {
        if (!StringUtils.hasText(policyId)) {
            throw new IllegalArgumentException("Policy ID cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "policies", policyId}, null);

                client.delete(url);
                log.info("Deleted policy: {}", policyId);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error deleting policy: " + policyId + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    @Override
    public void deleteUser(LFSConfig config, String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "users", userId}, null);

                client.delete(url);
                log.info("Deleted user: {}", userId);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error deleting user: " + userId + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    @Override
    public void deleteGroup(LFSConfig config, String groupId) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("Group ID cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "groups", groupId}, null);

                client.delete(url);
                log.info("Deleted group: {}", groupId);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error deleting group: " + groupId + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    @Override
    public void attachPolicyToGroup(LFSConfig config, String groupId, String policyId) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("Group ID cannot be empty");
        }
        if (!StringUtils.hasText(policyId)) {
            throw new IllegalArgumentException("Policy ID cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "groups", groupId, "policies"}, null);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("id", policyId);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Attached policy: {} to group: {}", policyId, groupId);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error attaching policy: " + policyId + " to group: " + groupId + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    @Override
    public void detachPolicyFromGroup(LFSConfig config, String groupId, String policyId) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("Group ID cannot be empty");
        }
        if (!StringUtils.hasText(policyId)) {
            throw new IllegalArgumentException("Policy ID cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "groups", groupId, "policies", policyId}, null);

                client.delete(url);
                log.info("Detached policy: {} from group: {}", policyId, groupId);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error detaching policy: " + policyId + " from group: " + groupId + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    @Override
    public void addGroupMember(LFSConfig config, String groupId, String userId) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("Group ID cannot be empty");
        }
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "groups", groupId, "members"}, null);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("id", userId);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                client.postForEntity(url, request, Map.class);
                log.info("Added user: {} to group: {}", userId, groupId);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error adding user: " + userId + " to group: " + groupId + ", HTTP status: " + e.getStatusCode(), e);
            }
            return null;
        });
    }

    @Override
    public void removeGroupMember(LFSConfig config, String groupId, String userId) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("Group ID cannot be empty");
        }
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        executeWithRetry(() -> {
            try {
                RestTemplate client = getConnection(config);
                String url = buildLakeFSUrl(config, new String[]{"auth", "groups", groupId, "members", userId}, null);

                client.delete(url);
                log.info("Removed user: {} from group: {}", userId, groupId);
            } catch (HttpClientErrorException e) {
                throw new LakeFSException("Error removing user: " + userId + " from group: " + groupId + ", HTTP status: " + e.getStatusCode(), e);
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
         * @throws LakeFSException the lake fs exception
         */
        T get() throws LakeFSException;
    }
}