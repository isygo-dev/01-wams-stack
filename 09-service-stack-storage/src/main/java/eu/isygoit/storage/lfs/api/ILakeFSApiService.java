package eu.isygoit.storage.lfs.api;

import eu.isygoit.enums.IEnumLogicalOperator;
import eu.isygoit.storage.exception.LakeFSObjectException;
import eu.isygoit.storage.lfs.config.LFSConfig;
import eu.isygoit.storage.s3.object.FileStorage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Interface defining operations for interacting with LakeFS data versioning system.
 */
public interface ILakeFSApiService {

    /**
     * Retrieves or creates a LakeFS client connection for the specified tenant.
     *
     * @param config Storage configuration containing tenant, credentials, and endpoint
     * @return LakeFS client instance
     * @throws LakeFSObjectException if connection creation fails
     */
    Object getConnection(LFSConfig config);

    /**
     * Updates the LakeFS client connection for a tenant.
     *
     * @param config Storage configuration
     * @throws LakeFSObjectException if connection update fails
     */
    void updateConnection(LFSConfig config);

    /**
     * Checks if a repository exists.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @return true if repository exists
     * @throws LakeFSObjectException on failure
     */
    boolean repositoryExists(LFSConfig config, String repositoryName);

    /**
     * Creates a repository if it doesn't exist.
     *
     * @param config           Storage configuration
     * @param repositoryName   Name of the repository
     * @param storageNamespace Storage namespace (e.g., s3://bucket-name)
     * @param defaultBranch    Default branch name (typically "main")
     * @throws LakeFSObjectException if repository creation fails
     */
    void createRepository(LFSConfig config, String repositoryName, String storageNamespace, String defaultBranch);

    /**
     * Checks if a branch exists in a repository.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @return true if branch exists
     * @throws LakeFSObjectException on failure
     */
    boolean branchExists(LFSConfig config, String repositoryName, String branchName);

    /**
     * Creates a new branch from an existing branch.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the new branch
     * @param sourceBranch   Source branch to create from
     * @throws LakeFSObjectException if branch creation fails
     */
    void createBranch(LFSConfig config, String repositoryName, String branchName, String sourceBranch);

    /**
     * Uploads a file to LakeFS with metadata.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param path           Object path
     * @param objectName     Object name
     * @param multipartFile  File to upload
     * @throws LakeFSObjectException if upload fails
     */
    void uploadFile(LFSConfig config, String repositoryName, String branchName, String path, String objectName,
                    MultipartFile multipartFile);

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
    byte[] getObject(LFSConfig config, String repositoryName, String reference, String objectName);

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
    String getPresignedObjectUrl(LFSConfig config, String repositoryName, String reference, String objectName);

    /**
     * Deletes an object from LakeFS.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param objectName     Object name
     * @throws LakeFSObjectException if deletion fails
     */
    void deleteObject(LFSConfig config, String repositoryName, String branchName, String objectName);

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
    List<FileStorage> getObjectByMetadata(LFSConfig config, String repositoryName, String reference,
                                          Map<String, String> metadata, IEnumLogicalOperator.Types condition);

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
    List<FileStorage> getObjects(LFSConfig config, String repositoryName, String reference, String prefix);

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
    void updateMetadata(LFSConfig config, String repositoryName, String branchName, String objectName, Map<String, String> metadata);

    /**
     * Deletes multiple objects from a branch.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param objectNames    List of object names to delete
     * @throws LakeFSObjectException if deletion fails
     */
    void deleteObjects(LFSConfig config, String repositoryName, String branchName, List<String> objectNames);

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
    String commit(LFSConfig config, String repositoryName, String branchName, String message, Map<String, String> metadata);

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
    String merge(LFSConfig config, String repositoryName, String sourceBranchName, String destinationBranchName, String message);

    /**
     * Lists all branches in a repository.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @return List of branch names
     * @throws LakeFSObjectException if listing fails
     */
    List<String> getBranches(LFSConfig config, String repositoryName);

    /**
     * Lists all repositories for the given configuration.
     *
     * @param config Storage configuration
     * @return List of repository names
     * @throws LakeFSObjectException if listing fails
     */
    List<String> getRepositories(LFSConfig config);

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
    List<Map<String, Object>> getCommitHistory(LFSConfig config, String repositoryName, String branchName, int limit);

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
    List<Map<String, Object>> getDiff(LFSConfig config, String repositoryName, String leftRef, String rightRef);

    /**
     * Reverts changes in a branch to a specific commit.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @param commitId       Commit ID to revert to
     * @throws LakeFSObjectException if revert fails
     */
    void revert(LFSConfig config, String repositoryName, String branchName, String commitId);

    /**
     * Deletes a branch from a repository.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @param branchName     Name of the branch
     * @throws LakeFSObjectException if branch deletion fails
     */
    void deleteBranch(LFSConfig config, String repositoryName, String branchName);

    /**
     * Deletes a repository.
     *
     * @param config         Storage configuration
     * @param repositoryName Name of the repository
     * @throws LakeFSObjectException if repository deletion fails
     */
    void deleteRepository(LFSConfig config, String repositoryName);
}