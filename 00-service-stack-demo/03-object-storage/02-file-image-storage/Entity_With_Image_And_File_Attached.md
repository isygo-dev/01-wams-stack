# Entity with Image and File Attached

This document describes a Spring Boot implementation for managing entities with both attached images and files in a
multitenant environment. The example focuses on a `Resume` entity with support for CRUD operations, image handling (
e.g., JPEG), and file handling (e.g., PDF). It uses Spring Boot, JPA, Testcontainers for integration testing, and
MapStruct for entity-DTO mapping. The system ensures tenant isolation and includes comprehensive integration tests.

## Overview

The implementation provides a RESTful API for managing resume entities, including the ability to attach and retrieve
images and files. It leverages Spring Boot for the backend, PostgreSQL (via Testcontainers) for data persistence, and
MapStruct for mapping between entities and DTOs. The system supports multitenancy and includes tests for CRUD
operations, image and file handling, and edge cases.

## Components

### 1. ResumeController.java

The `ResumeController` is a REST controller handling standard CRUD operations for resumes. It extends
`MappedCrudTenantController` to provide tenant-aware endpoints.

- **Key Features**:
    - Manages resume creation, retrieval, update, and deletion.
    - Enforces tenant isolation using the `X-Tenant-ID` header.
    - Uses `@CrossOrigin` for local development (CORS).
    - Injects `ResumeMapper` and `ResumeService` via custom annotations.

### 2. ResumeFileController.java

The `ResumeFileController` extends `MappedFileTenantController` to handle file-specific operations (upload and download)
for resumes.

- **Key Features**:
    - Supports file upload and download endpoints (e.g., PDFs).
    - Integrates with `ResumeService` for file storage and retrieval.
    - Maintains tenant isolation.

### 3. ResumeImageController.java

The `ResumeImageController` extends `MappedImageTenantController` to handle image-specific operations (upload and
download) for resumes.

- **Key Features**:
    - Supports image upload and download endpoints (e.g., JPEG).
    - Integrates with `ResumeService` for image storage and retrieval.
    - Maintains tenant isolation.

### 4. ResumeEntity.java

The `ResumeEntity` represents the resume table (`RESUME`) in the database, with file metadata stored in a secondary
table (`RESUME_FILE`). It extends `AuditableEntity` and implements `ITenantAssignable`, `IImageEntity`, `IFileEntity`,
and `ICodeAssignable`.

- **Key Features**:
    - Fields: `id`, `tenant`, `code`, `title`, `description`, `startDate`, `endDate`, `active`, `imagePath`, `fileName`,
      `originalFileName`, `path`, `extension`, `type`, `tags`.
    - `@Id` with sequence-based ID generation.
    - `@Criteria` on `title`, `description`, `startDate`, and `endDate` for filtering.
    - Image storage via `imagePath`.
    - File metadata stored in `RESUME_FILE` secondary table.
    - Supports code generation for unique identifiers (e.g., `RES000001`).
    - Includes a list of tags for file categorization.

### 5. ResumeFileEntity.java

The `ResumeFileEntity` extends `FileEntity` and implements `IFileEntity` to represent file-specific data in the
`RESUME_FILE` table.

- **Key Features**:
    - Fields: `id`, `tags`.
    - Uses sequence-based ID generation.
    - Stores file tags in a separate `RESUME_FILE_TAGS` table.

### 6. ResumeRepository.java

The `ResumeRepository` is a JPA repository extending `JpaPagingAndSortingTenantAndCodeAssignableRepository` to support
tenant-aware and code-aware queries with pagination and sorting.

- **Key Features**:
    - Provides CRUD operations for `ResumeEntity`.
    - Supports tenant and code-based filtering.

### 7. ResumeService.java

The `ResumeService` handles business logic for resume, image, and file operations. It extends `FileImageTenantService`
and uses `ResumeRepository` for persistence.

- **Key Features**:
    - Transactional operations with `@Transactional`.
    - Injects `ResumeRepository` and code generator via custom annotations.
    - Configures storage directory (`/resume`).
    - Initializes code generation for unique resume codes (e.g., `RES000001`).
    - Logs operations using SLF4J.

### 8. ResumeDto.java

The `ResumeDto` is a data transfer object for resume data, extending `AbstractAuditableDto` and implementing
`IFileUploadDto` and `IImageUploadDto`.

- **Key Features**:
    - Fields: `tenant`, `code`, `title`, `description`, `startDate`, `endDate`, `active`, `originalFileName`,
      `imagePath`.
    - Supports both file and image uploads.

### 9. ResumeMapper.java

The `ResumeMapper` is a MapStruct interface for mapping between `ResumeEntity` and `ResumeDto`.

- **Key Features**:
    - Uses Spring component model.
    - Applies null checks with `NullValueCheckStrategy.ALWAYS`.

### 10. ImageFileCrudIntegrationTests.java

The integration tests validate resume, image, and file operations using Testcontainers with a PostgreSQL database. Tests
cover CRUD, image/file upload/download, and edge cases.

- **Key Features**:
    - Uses Testcontainers for PostgreSQL setup.
    - Tests resume creation with images and files.
    - Validates image/file upload and download.
    - Ensures data integrity and tenant isolation.

## Setup and Dependencies

- **Spring Boot**: Application framework.
- **JPA/Hibernate**: Database interactions.
- **Testcontainers**: Manages PostgreSQL for testing.
- **MapStruct**: Entity-DTO mapping.
- **Lombok**: Reduces boilerplate code.
- **Jackson**: JSON serialization/deserialization.
- **PostgreSQL**: Database for resume, image, and file data.

### Prerequisites

- Java 17 or later.
- Maven for dependency management.
- Docker for Testcontainers.

### Configuration

- **Database**: PostgreSQL 15 (via Testcontainers).
- **Multitenancy**: Uses GDM mode with tenant-specific schemas.
- **Properties**:
    - `spring.jpa.hibernate.ddl-auto=create`
    - `multitenancy.mode=GDM`
    - Dynamic datasource configuration for tenants.

## Running the Application

1. **Clone the Repository** (if applicable).
2. **Install Dependencies**:
   ```bash
   mvn clean install
   ```
3. **Run Tests**:
   ```bash
   mvn test
   ```
   Testcontainers starts a PostgreSQL container for integration tests.

4. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```
   The API is available at `http://localhost:8080/api/v1/resume`.

## API Endpoints

- **POST /api/v1/resume**: Create a resume without image or file.
- **POST /api/v1/resume/file**: Create a resume with a file (multipart).
- **POST /api/v1/resume/image**: Create a resume with an image (multipart).
- **GET /api/v1/resume**: List all resumes (paged or unpaged).
- **GET /api/v1/resume/{id}**: Retrieve a resume by ID.
- **GET /api/v1/resume/file/download/{id}**: Download a resume’s file.
- **GET /api/v1/resume/image/download/{id}**: Download a resume’s image.
- **PUT /api/v1/resume/{id}**: Update a resume without image or file.
- **PUT /api/v1/resume/file/{id}**: Update a resume with a file (multipart).
- **PUT /api/v1/resume/image/{id}**: Update a resume with an image (multipart).
- **PUT /api/v1/resume/file/upload/{id}**: Upload a file for an existing resume.
- **PUT /api/v1/resume/image/upload/{id}**: Upload an image for an existing resume.
- **DELETE /api/v1/resume/{id}**: Delete a resume.

**Headers**:

- `X-Tenant-ID`: Specifies the tenant (e.g., `tenants`).

## Testing Scenarios

The integration tests cover:

1. Creating resumes with images and files.
2. Updating resumes with images and files.
3. Uploading and downloading images and files.
4. Edge cases: invalid DTOs, non-existent resumes, and file/image format validation.

## Example Usage

### Create a Resume Without Image or File

```bash
curl -X POST http://localhost:8080/api/v1/resume \
-H "Content-Type: application/json" \
-H "X-Tenant-ID: tenants" \
-d '{
    "tenant": "tenants",
    "code": "RES001",
    "title": "Test Resume",
    "description": "Test Description",
    "startDate": "2025-07-22T12:00:00",
    "endDate": "2025-10-22T12:00:00",
    "active": true
}'
```

### Create a Resume With File

```bash
curl -X POST http://localhost:8080/api/v1/resume/file \
-H "X-Tenant-ID: tenants" \
-F "dto={\"tenant\":\"tenants\",\"code\":\"RES002\",\"title\":\"Resume with File\",\"description\":\"File upload test\",\"startDate\":\"2025-07-22T12:00:00\",\"endDate\":\"2025-10-22T12:00:00\",\"active\":true};type=application/json" \
-F "file=@resume.pdf;type=application/pdf"
```

### Create a Resume With Image

```bash
curl -X POST http://localhost:8080/api/v1/resume/image \
-H "X-Tenant-ID: tenants" \
-F "dto={\"tenant\":\"tenants\",\"code\":\"RES003\",\"title\":\"Resume with Image\",\"description\":\"Image upload test\",\"startDate\":\"2025-07-22T12:00:00\",\"endDate\":\"2025-10-22T12:00:00\",\"active\":true};type=application/json" \
-F "file=@profile.jpg;type=image/jpeg"
```

### Download a Resume’s File

```bash
curl -X GET http://localhost:8080/api/v1/resume/file/download/1?version=0 \
-H "X-Tenant-ID: tenants" \
--output downloaded_resume.pdf
```

### Download a Resume’s Image

```bash
curl -X GET http://localhost:8080/api/v1/resume/image/download/1 \
-H "X-Tenant-ID: tenants" \
--output downloaded_profile.jpg
```

### List Resumes (Paginated)

```bash
curl -X GET "http://localhost:8080/api/v1/resume?page=0&size=10" \
-H "X-Tenant-ID: tenants"
```

## Notes

- **Tenant Isolation**: Ensured via `tenant` field and `X-Tenant-ID` header.
- **Image Handling**: Images (e.g., JPEG) are stored with paths in `/resume`.
- **File Handling**: Files (e.g., PDF) are stored with metadata in `/resume`, using the `RESUME_FILE` secondary table.
- **Code Generation**: Unique codes (e.g., `RES000001`) are generated for resumes.
- **Secondary Table**: File metadata is stored in `RESUME_FILE` with tags in `RESUME_FILE_TAGS`.
- **Testcontainers**: Eliminates external database dependencies for testing.
- **Extensibility**: The system can be extended for additional features like file versioning or advanced metadata.

For further details, refer to the provided source code files.