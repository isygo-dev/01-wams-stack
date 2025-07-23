# S3-Compatible and Data Versioning Storage Solutions

This repository contains Java service implementations for interacting with S3-compatible object storage systems (Ceph,
Garage, MinIO, OxiCloud) and a data versioning platform (LakeFS). Below is a table of the top 10 open-source
S3-compatible storage solutions, followed by a separate section for LakeFS, which uses a distinct Git-like interface for
data versioning. The implemented service files are described in detail afterward.

## Top 10 Open-Source S3-Compatible Storage Solutions

The following table lists the top 10 open-source S3-compatible storage solutions, highlighting their key features,
licenses, advantages, and limitations. These solutions are selected based on their S3 API compatibility, performance,
scalability, and community adoption.

| **Solution**   | **Key Features**                                                                | **License**        | **Pros**                                                 | **Cons**                                              |
|----------------|---------------------------------------------------------------------------------|--------------------|----------------------------------------------------------|-------------------------------------------------------|
| **MinIO**      | High-performance, Kubernetes-native, active-active replication, erasure coding  | GNU AGPL v3        | Simple deployment, excellent performance, widely adopted | Requires infrastructure setup for self-hosting        |
| **Ceph**       | Unified storage (object, block, file), RADOS Gateway, data replication, tiering | LGPL v2.1          | Robust, scalable, self-healing architecture              | Complex setup and management                          |
| **Storj**      | Decentralized, client-side encryption, S3 API, Veeam Ready                      | AGPL v3            | No vendor lock-in, eco-friendly, secure                  | Potential latency in decentralized model              |
| **OpenIO**     | Grid-based, hybrid/multi-cloud, event-driven processing                         | AGPL v3            | Lightweight, flexible for diverse use cases              | Less community traction than MinIO or Ceph            |
| **Zenko**      | Multi-cloud data controller, S3 API, data orchestration                         | Apache License 2.0 | Ideal for multi-cloud, avoids vendor lock-in             | Setup complexity, limited adoption                    |
| **SwiftStack** | Geo-distribution, S3 compatibility, OpenStack integration                       | Apache License 2.0 | Enterprise-ready, robust management tools                | Less focus on S3 compared to MinIO, OpenStack-centric |
| **SeaweedFS**  | Object/file/POSIX storage, erasure coding, replication                          | Apache License 2.0 | Easy to deploy, good for small/medium setups             | Less mature S3 compatibility                          |
| **Riak CS**    | Distributed, no single point of failure, strong consistency                     | Apache License 2.0 | Reliable for distributed environments                    | Slowed development, limited community support         |
| **Garage**     | Lightweight, geo-replicated, S3 API, self-hosted                                | AGPL v3            | Simple, lightweight, ideal for self-hosted setups        | Younger project, fewer features than MinIO            |
| **LeoFS**      | Multi-protocol (S3, REST, NFS), high availability, auto-rebalancing             | Apache License 2.0 | Good for large-scale, high-availability setups           | Complex configuration, less community traction        |

### Notes on S3-Compatible Storage Solutions

- **Selection Criteria**: These solutions were chosen for their full compatibility with the S3 API, performance
  metrics (e.g., MinIO's 325 GiB/s GET throughput), scalability, and relevance in open-source ecosystems. MinIO and Ceph
  are leaders due to their maturity, while Garage and SeaweedFS are emerging for lightweight setups.
- **Use Case Considerations**:
    - **MinIO**: Ideal for high-performance AI/ML and cloud-native workloads.
    - **Ceph**: Best for unified storage across object, block, and file systems.
    - **Storj**: Suited for decentralized, cost-effective storage.
    - **Garage**: Lightweight and ideal for self-hosted, geo-distributed setups.
- **Sources**: Information is derived from official documentation and community discussions for each solution.
-

## Implemented Service Files

This repository includes Java service implementations for four S3-compatible storage systems (Ceph, Garage, MinIO,
OxiCloud) and one data versioning platform (LakeFS). Each service handles common operations like bucket/repository
management, file uploads, object retrieval, tagging/metadata, and deletion, with robust error handling, connection
pooling, and retry logic. Below is a detailed overview of each implemented file.

### CephApiService.java

- **Purpose**: Interfaces with Ceph’s RADOS Gateway (RGW) using the AWS S3 SDK for S3-compatible object storage
  operations.
- **Key Features**:
    - Manages tenant-specific S3 client connections with endpoint and credential configuration.
    - Supports bucket operations (create, delete, check existence, versioning).
    - Handles file operations (upload, retrieve, delete, presigned URLs).
    - Provides tag-based object retrieval with AND/OR logical conditions.
    - Implements retry logic (3 attempts, 1-second delay) for robust error handling.
- **Implementation Details**:
    - Uses `S3Client` from the AWS SDK with path-style access enabled.
    - Validates inputs (e.g., bucket names, object names, configuration).
    - Logs operations and errors using SLF4J.
    - Throws `S3BuketException` for Ceph-specific errors.
- **Use Case**: Suitable for enterprise-grade deployments requiring unified storage and high scalability.

### GarageApiService.java

- **Purpose**: Interacts with Garage, a lightweight, S3-compatible, geo-distributed storage system.
- **Key Features**:
    - Supports bucket and object operations (create, delete, upload, retrieve, presigned URLs).
    - Manages tenant-specific S3 client connections.
    - Provides tag-based object filtering with AND/OR conditions.
    - Includes retry logic and input validation.
- **Implementation Details**:
    - Uses `S3Client` from the AWS SDK with path-style access, similar to Ceph.
    - Throws `S3BuketException` for error handling.
        - Simplified versioning support (assumes versioning is not enabled by default).
- **Use Case**: Ideal for self-hosted, lightweight, and geo-distributed storage setups.

### LakeFSApiService.java

- **Purpose**: Interfaces with LakeFS, a data versioning platform with a REST-based API, focusing on repository, branch,
  and commit management.
- **Key Features**:
    - Supports repository operations (create, delete, list) and branch management (create, delete, list).
    - Handles file operations (upload, retrieve, delete) with versioning via branches or commits.
    - Provides commit, merge, revert, and diff operations for data versioning.
    - Supports metadata-based object filtering with AND/OR conditions.
    - Generates presigned URLs for object access.
- **Implementation Details**:
    - Uses `RestTemplate` with Basic Authentication, reflecting LakeFS’s HTTP-based API (not S3-native).
    - Handles LakeFS-specific concepts like repositories, branches, and commits.
    - Includes retry logic (3 attempts, 1-second delay) and validation for repository, branch, and object parameters.
    - Throws `LakeFSObjectException` for error handling.
- **Use Case**: Best for data versioning, reproducibility, and ML/AI workflows requiring Git-like control over data.

### MinIOApiService.java

- **Purpose**: Interfaces with MinIO, a high-performance, S3-compatible object storage system.
- **Key Features**:
    - Supports bucket operations (create, delete, check existence, versioning).
    - Handles file operations (upload, retrieve, delete, presigned URLs) with tag support.
    - Retrieves objects by tags with AND/OR logical conditions.
    - Includes retry logic and connection pooling for tenant-specific clients.
- **Implementation Details**:
    - Uses the MinIO Java SDK (`MinioClient`) for optimized MinIO interactions.
    - Supports versioning with explicit version ID handling in object operations.
    - Validates inputs and throws `MinIoObjectException` for errors.
    - Logs operations using SLF4J for debugging and monitoring.
- **Use Case**: Perfect for cloud-native applications, AI/ML workloads, and high-throughput storage needs.

### OxiCloudApiService.java

- **Purpose**: Interfaces with OxiCloud, an S3-compatible storage solution (assumed to be a placeholder or
  less-documented system).
- **Key Features**:
    - Supports bucket and object operations (create, delete, upload, retrieve, presigned URLs).
    - Provides tag-based object retrieval with AND/OR conditions.
    - Includes retry logic and tenant-specific connection management.
- **Implementation Details**:
    - Uses `S3Client` from the AWS SDK with path-style access, similar to Ceph and Garage.
    - Throws `OxiCloudObjectException` for error handling.
    - Assumes minimal versioning support, similar to Garage.
- **Use Case**: Suitable for environments using OxiCloud or similar S3-compatible systems with standard object storage
  needs.

## Implementation Notes

- **Common Features**:
    - All services implement retry logic (3 attempts, 1-second delay) to handle transient failures.
    - Connection pooling manages tenant-specific clients for performance.
    - Input validation ensures robust error handling for bucket/repository names, object names, and configurations.
    - Logging (via SLF4J) provides detailed operation tracking and error reporting.
- **Differences**:
    - `MinIOApiService` uses the MinIO SDK for optimized interactions, while Ceph, Garage, and OxiCloud use the AWS S3
      SDK.
    - `CephApiService`, `GarageApiService`, and `OxiCloudApiService` are structurally similar, differing mainly in
      exception types.
- **Dependencies**:
    - AWS SDK for Java (`software.amazon.awssdk`) for Ceph, Garage, and OxiCloud.
    - MinIO Java SDK (`io.minio`) for MinIO.
    - Spring Framework (`RestTemplate`, `MultipartFile`) for HTTP and file handling in LakeFS.
    - Apache Commons IO for stream utilities.
    - Lombok for logging (`@Slf4j`) and boilerplate reduction.
- **Extensibility**:
    - Abstract classes can be extended to customize behavior or add functionality.
    - Interfaces (`ICephApiService`, `IGarageApiService`, etc.) ensure consistent APIs.

## Getting Started

1. **Setup Dependencies**: Include dependencies (AWS SDK, MinIO SDK, Spring Framework, Apache Commons IO, Lombok) in
   your project’s build file (e.g., `pom.xml` for Maven).
2. **Configure Storage**:
    - Provide a `StorageConfig` object with tenant, URL, username, and password.
    - Example: `StorageConfig config = new StorageConfig("tenant1", "http://minio:9000", "user", "password");`
3. **Extend Services**: Implement the abstract classes (e.g., `extends CephApiService`) and inject the appropriate
   client map (`Map<String, S3Client>` or `Map<String, MinioClient>`).
4. **Usage Example (S3-Compatible)**:
   ```java
   CephApiService cephService = new MyCephApiService(s3ClientMap);
   cephService.makeBucket(config, "my-bucket");
   cephService.uploadFile(config, "my-bucket", "path/to", "file.txt", file, Map.of("key", "value"));
   byte[] fileContent = cephService.getObject(config, "my-bucket", "path/to/file.txt", null);
   ```

## Contributing

Contributions are welcome! To add support for other S3-compatible solutions (e.g., SeaweedFS, Riak CS) or enhance LakeFS
functionality:

1. Create a new service class following the pattern of existing implementations.
2. Ensure compatibility with `StorageConfig` and `FileStorage` models.
3. Add unit tests to validate functionality.
4. Update this README with details of the new implementation.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.