# Entity with Multiple File Attached

This example demonstrates a Spring Boot application implementing a multi-tenant system for managing resume entities with
multiple file attachments. The code includes entity definitions, data transfer objects (DTOs), mappers, services,
controllers, and comprehensive integration tests.

## Overview

The application allows users to:

- Create resume entities with associated metadata
- Upload multiple files associated with a resume
- Download attached files
- Delete attached files
- Handle multi-tenancy with tenant-specific data isolation

## Key Components

### 1. Entity Definitions

#### ResumeEntity

- Represents the main resume entity
- Stores metadata like title, description, start/end dates
- Supports file and image uploads

#### ResumeLinkedFile

- Represents individual file attachments
- Linked to a parent ResumeEntity
- Stores file metadata (original name, size, version)
- Implements tenant assignment with `TENANT_ID` column

### 2. Data Transfer Objects (DTOs)

#### ResumeDto

- Extends `AbstractAuditableDto`
- Implements `IFileUploadDto` and `IImageUploadDto`
- Contains fields for resume metadata and file/image paths

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ResumeDto extends AbstractAuditableDto<Long> implements IFileUploadDto, IImageUploadDto {
    private String tenant;
    private String code;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private String originalFileName;
    private String imagePath;
}
```

#### ResumeLinkedFileDto

- Extends `LinkedFileMinDto`
- Represents file attachment metadata

```java
@Data
@AllArgsConstructor
@SuperBuilder
public class ResumeLinkedFileDto extends LinkedFileMinDto<Long> {
}
```

### 3. Mappers

#### ResumeMapper

- Maps between `ResumeEntity` and `ResumeDto`
- Uses MapStruct with Spring component model
- Implements null value checking

#### ResumeLinkedFileMapper

- Maps between `ResumeLinkedFile` and `ResumeLinkedFileDto`
- Similar MapStruct configuration

### 4. Services

#### ResumeService

- Handles core resume operations
- Manages file/image uploads
- Implements code generation for unique identifiers
- Configures upload directory as `/resume`

#### ResumeMultiFileService

- Extends `MultiFileTenantService`
- Manages multiple file attachments
- Configures upload directory as `/resume/files`
- Generates unique codes with "RLF" prefix

### 5. Controller

#### ResumeMultiFileController

- REST controller for file operations
- Supports CRUD operations for file attachments
- Handles multi-tenant requests with `X-Tenant-ID` header
- Configured for CORS with localhost:8081

```java
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api/v1/resume")
public class ResumeMultiFileController extends MappedMultiFileTenatController<Long, ResumeEntity, ResumeLinkedFileDto, ResumeDto, ResumeDto, ResumeMultiFileService> implements IMappedMultiFileApi<ResumeLinkedFileDto, Long> {
}
```

### 6. Repository

#### ResumeLinkedFileRepository

- Extends `JpaPagingAndSortingTenantAssignableRepository`
- Provides data access for `ResumeLinkedFile` entities
- Supports pagination and tenant-specific queries

### 7. Integration Tests

#### MultiFileCrudIntegrationTests

- Comprehensive test suite using Testcontainers with PostgreSQL
- Tests CRUD operations for file attachments
- Covers edge cases (invalid IDs, empty files, missing tenant headers)
- Tests file upload/download with different file types
- Verifies tenant isolation

```java
@SpringBootTest
@ActiveProfiles("postgres")
@AutoConfigureMockMvc
@Testcontainers
class MultiFileCrudIntegrationTests {
    // Tests include:
    // - Resume creation
    // - Multiple file uploads
    // - Single file upload
    // - File download
    // - File deletion
    // - Error cases (invalid IDs, empty files)
    // - Different file types
    // - Large file handling
}
```

## Key Features

- **Multi-Tenancy**: Uses `X-Tenant-ID` header for tenant isolation
- **File Management**: Supports multiple file uploads with different MIME types
- **Error Handling**: Proper validation for empty files, invalid IDs
- **Code Generation**: Automatic unique code generation for entities
- **Testing**: Comprehensive integration tests with Testcontainers
- **REST API**: Standard endpoints for CRUD operations
- **CORS**: Configured for cross-origin requests

## API Endpoints

- `POST /api/v1/resume`: Create a resume
- `PUT /api/v1/resume/multi-files/upload`: Upload multiple files
- `PUT /api/v1/resume/multi-files/upload/one`: Upload single file
- `GET /api/v1/resume/multi-files/download`: Download file
- `DELETE /api/v1/resume/multi-files`: Delete file

## Setup Requirements

- Java 17+
- Spring Boot 3.x
- PostgreSQL 15 (via Testcontainers for testing)
- MapStruct for entity mapping
- Lombok for boilerplate reduction
- Spring Data JPA for persistence

## Usage Example

1. Create a resume:

```http
POST /api/v1/resume
X-Tenant-ID: tenants
Content-Type: application/json
{
    "title": "Test Resume",
    "description": "Test Description",
    "startDate": "2025-07-23T12:00:00",
    "endDate": "2025-08-23T12:00:00"
}
```

2. Upload files:

```http
PUT /api/v1/resume/multi-files/upload?parentId=1
X-Tenant-ID: tenants
Content-Type: multipart/form-data
```

3. Download a file:

```http
GET /api/v1/resume/multi-files/download?parentId=1&fileId=1&version=1
X-Tenant-ID: tenants
```

## Notes

- The application uses a sequence generator for IDs
- File storage is configured with specific directories
- Tenant schema is initialized before tests
- Comprehensive error handling for invalid inputs
- Supports various file types (text, PDF, DOCX, JSON, XML)

This implementation provides a robust foundation for managing entities with multiple file attachments in a multi-tenant
environment, with thorough testing and proper error handling.