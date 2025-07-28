package eu.isygoit.lfs.api;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.lfs.config.LFSConfig;
import eu.isygoit.s3.object.FileStorage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * The interface Lake fs api service.
 */
public interface ILakeFSApiService {

    /**
     * Sets lake fs.
     *
     * @param config    the config
     * @param username  the username
     * @param accessKey the access key
     * @param secretKey the secret key
     */
    void setupLakeFS(LFSConfig config, String username, String accessKey, String secretKey);

    /**
     * Gets connection.
     *
     * @param config the config
     * @return the connection
     */
    Object getConnection(LFSConfig config);

    /**
     * Update connection.
     *
     * @param config the config
     */
    void updateConnection(LFSConfig config);

    /**
     * Repository exists boolean.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @return the boolean
     */
    boolean repositoryExists(LFSConfig config, String repositoryName);

    /**
     * Create repository.
     *
     * @param config           the config
     * @param repositoryName   the repository name
     * @param storageNamespace the storage namespace
     * @param defaultBranch    the default branch
     */
    void createRepository(LFSConfig config, String repositoryName, String storageNamespace, String defaultBranch);

    /**
     * Branch exists boolean.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param branchName     the branch name
     * @return the boolean
     */
    boolean branchExists(LFSConfig config, String repositoryName, String branchName);

    /**
     * Create branch.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param branchName     the branch name
     * @param sourceBranch   the source branch
     */
    void createBranch(LFSConfig config, String repositoryName, String branchName, String sourceBranch);

    /**
     * Upload file.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param branchName     the branch name
     * @param path           the path
     * @param objectName     the object name
     * @param file           the multipart file
     */
    void uploadFile(LFSConfig config, String repositoryName, String branchName, String path, String objectName,
                    MultipartFile file);

    /**
     * Get object byte [ ].
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param reference      the reference
     * @param objectName     the object name
     * @return the byte [ ]
     */
    byte[] getObject(LFSConfig config, String repositoryName, String reference, String objectName);

    /**
     * Gets presigned object url.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param reference      the reference
     * @param objectName     the object name
     * @param expiryHours    the expiry hours
     * @return the presigned object url
     */
    String getPresignedObjectUrl(LFSConfig config, String repositoryName, String reference, String objectName, int expiryHours);

    /**
     * Delete object.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param branchName     the branch name
     * @param objectName     the object name
     */
    void deleteObject(LFSConfig config, String repositoryName, String branchName, String objectName);

    /**
     * Gets object by metadata.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param reference      the reference
     * @param metadata       the metadata
     * @param condition      the condition
     * @return the object by metadata
     */
    List<FileStorage> getObjectByMetadata(LFSConfig config, String repositoryName, String reference,
                                          Map<String, String> metadata, IEnumLogicalOperator.Types condition);

    /**
     * Gets objects.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param reference      the reference
     * @param prefix         the prefix
     * @return the objects
     */
    List<FileStorage> getObjects(LFSConfig config, String repositoryName, String reference, String prefix);

    /**
     * Update metadata.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param branchName     the branch name
     * @param objectName     the object name
     * @param metadata       the metadata
     */
    void updateMetadata(LFSConfig config, String repositoryName, String branchName, String objectName, Map<String, String> metadata);

    /**
     * Delete objects.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param branchName     the branch name
     * @param objectNames    the object names
     */
    void deleteObjects(LFSConfig config, String repositoryName, String branchName, List<String> objectNames);

    /**
     * Commit string.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param branchName     the branch name
     * @param message        the message
     * @param metadata       the metadata
     * @return the string
     */
    String commit(LFSConfig config, String repositoryName, String branchName, String message, Map<String, String> metadata);

    /**
     * Merge string.
     *
     * @param config                the config
     * @param repositoryName        the repository name
     * @param sourceBranchName      the source branch name
     * @param destinationBranchName the destination branch name
     * @param message               the message
     * @return the string
     */
    String merge(LFSConfig config, String repositoryName, String sourceBranchName, String destinationBranchName, String message);

    /**
     * Gets branches.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @return the branches
     */
    List<String> getBranches(LFSConfig config, String repositoryName);

    /**
     * Gets repositories.
     *
     * @param config the config
     * @return the repositories
     */
    List<String> getRepositories(LFSConfig config);

    /**
     * Gets commit history.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param branchName     the branch name
     * @param limit          the limit
     * @return the commit history
     */
    List<Map<String, Object>> getCommitHistory(LFSConfig config, String repositoryName, String branchName, int limit);

    /**
     * Gets diff.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param leftRef        the left ref
     * @param rightRef       the right ref
     * @return the diff
     */
    List<Map<String, Object>> getDiff(LFSConfig config, String repositoryName, String leftRef, String rightRef);

    /**
     * Revert.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param branchName     the branch name
     * @param commitId       the commit id
     */
    void revert(LFSConfig config, String repositoryName, String branchName, String commitId);

    /**
     * Delete branch.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param branchName     the branch name
     */
    void deleteBranch(LFSConfig config, String repositoryName, String branchName);

    /**
     * Delete repository.
     *
     * @param config         the config
     * @param repositoryName the repository name
     * @param force          the force
     */
    void deleteRepository(LFSConfig config, String repositoryName, boolean force);

    /*
     * Interface for LakeFS Auth API operations.
     */

    /**
     * Gets policy.
     *
     * @param config   the config
     * @param policyId the policy id
     * @return the policy
     */
    Map<String, Object> getPolicy(LFSConfig config, String policyId);

    /**
     * Gets user.
     *
     * @param config the config
     * @param userId the user id
     * @return the user
     */
    Map<String, Object> getUser(LFSConfig config, String userId);

    /**
     * List group members list.
     *
     * @param config  the config
     * @param groupId the group id
     * @param after   the after
     * @param amount  the amount
     * @return the list
     */
    List<String> listGroupMembers(LFSConfig config, String groupId, String after, int amount);

    /**
     * List group policies list.
     *
     * @param config  the config
     * @param groupId the group id
     * @param after   the after
     * @param amount  the amount
     * @return the list
     */
    List<String> listGroupPolicies(LFSConfig config, String groupId, String after, int amount);

    /**
     * List groups list.
     *
     * @param config the config
     * @param after  the after
     * @param amount the amount
     * @return the list
     */
    List<String> listGroups(LFSConfig config, String after, int amount);

    /**
     * List policies list.
     *
     * @param config the config
     * @param after  the after
     * @param amount the amount
     * @return the list
     */
    List<String> listPolicies(LFSConfig config, String after, int amount);

    /**
     * List users list.
     *
     * @param config the config
     * @param after  the after
     * @param amount the amount
     * @return the list
     */
    List<String> listUsers(LFSConfig config, String after, int amount);

    /**
     * Create policy.
     *
     * @param config    the config
     * @param policyId  the policy id
     * @param statement the statement
     */
    void createPolicy(LFSConfig config, String policyId, List<Map<String, Object>> statement);

    /**
     * Is user exists boolean.
     *
     * @param config the config
     * @param userId the user id
     * @return the boolean
     */
    boolean isUserExists(LFSConfig config, String userId);

    /**
     * Create user.
     *
     * @param config the config
     * @param userId the user id
     */
    void createUser(LFSConfig config, String userId);

    /**
     * Create group.
     *
     * @param config  the config
     * @param groupId the group id
     */
    void createGroup(LFSConfig config, String groupId);

    /**
     * Delete policy.
     *
     * @param config   the config
     * @param policyId the policy id
     */
    void deletePolicy(LFSConfig config, String policyId);

    /**
     * Delete user.
     *
     * @param config the config
     * @param userId the user id
     */
    void deleteUser(LFSConfig config, String userId);

    /**
     * Delete group.
     *
     * @param config  the config
     * @param groupId the group id
     */
    void deleteGroup(LFSConfig config, String groupId);

    /**
     * Attach policy to group.
     *
     * @param config   the config
     * @param groupId  the group id
     * @param policyId the policy id
     */
    void attachPolicyToGroup(LFSConfig config, String groupId, String policyId);

    /**
     * Detach policy from group.
     *
     * @param config   the config
     * @param groupId  the group id
     * @param policyId the policy id
     */
    void detachPolicyFromGroup(LFSConfig config, String groupId, String policyId);

    /**
     * Add group member.
     *
     * @param config  the config
     * @param groupId the group id
     * @param userId  the user id
     */
    void addGroupMember(LFSConfig config, String groupId, String userId);

    /**
     * Remove group member.
     *
     * @param config  the config
     * @param groupId the group id
     * @param userId  the user id
     */
    void removeGroupMember(LFSConfig config, String groupId, String userId);
}