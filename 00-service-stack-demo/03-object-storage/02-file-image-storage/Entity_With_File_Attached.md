# Entity with File Attached

This document outlines a Spring Boot implementation for managing entities with attached files in a multitenant environment. The example focuses on a `Contract` entity with support for CRUD operations and file handling (upload and download). It uses Spring Boot, JPA, Testcontainers for integration testing, and MapStruct for entity-DTO mapping. The system ensures tenant isolation and includes comprehensive integration tests.

## Overview

The implementation provides a RESTful API for managing contract entities, including the ability to attach and retrieve files (e.g., PDFs). It leverages Spring Boot for the backend, PostgreSQL (via Testcontainers) for data persistence, and MapStruct for mapping between entities and DTOs. The system supports multitenancy and includes tests for CRUD operations, file handling, and edge cases.

## Components

### 1. ContractController.java
The `ContractController` is a REST controller handling standard CRUD operations for contracts. It extends `MappedCrudTenantController` to provide tenant-aware endpoints.

- **Key Features**:
  - Manages contract creation, retrieval, update, and deletion.
  - Enforces tenant isolation using the `X-Tenant-ID` header.
  - Uses `@CrossOrigin` for local development (CORS).
  - Injects `ContractMapper` and `ContractService` via custom annotations.

### 2. ContractFileController.java
The `ContractFileController` extends `MappedFileTenantController` to handle file-specific operations (upload and download) for contracts.

- **Key Features**:
  - Supports file upload and download endpoints.
  - Integrates with `ContractService` for file storage and retrieval.
  - Maintains tenant isolation.

### 3. ContractEntity.java
The `ContractEntity` represents the contract table (`CONTRACT`) in the database, with file metadata stored in a secondary table (`CONTRACT_FILE`). It extends `AuditableEntity` and implements `ITenantAssignable`, `IFileEntity`, and `ICodeAssignable`.

- **Key Features**:
  - Fields: `id`, `tenant`, `code`, `title`, `description`, `startDate`, `endDate`, `active`, `fileName`, `originalFileName`, `path`, `extension`, `type`, `tags`.
  - `@Id` with sequence-based ID generation.
  - `@Criteria` on `title`, `description`, `startDate`, and `endDate` for filtering.
  - File metadata stored in `CONTRACT_FILE` secondary table.
  - Supports code generation for unique identifiers (e.g., `CTR000001`).
  - Includes a list of tags for file categorization.

### 4. ContractFileEntity.java
The `ContractFileEntity` extends `FileEntity` and implements `IFileEntity` to represent file-specific data in the `CONTRACT_FILE` table.

- **Key Features**:
  - Fields: `id`, `tags`.
  - Uses sequence-based ID generation.
  - Stores file tags in a separate `CONTRACT_FILE_TAGS` table.

### 5. ContractRepository.java
The `ContractRepository` is a JPA repository extending `JpaPagingAndSortingTenantAndCodeAssignableRepository` to support tenant-aware and code-aware queries with pagination and sorting.

- **Key Features**:
  - Provides CRUD operations for `ContractEntity`.
  - Supports tenant and code-based filtering.

### 6. ContractService.java
The `ContractService` handles business logic for contract and file operations. It extends `FileTenantService` and uses `ContractRepository` for persistence.

- **Key Features**:
  - Transactional operations with `@Transactional`.
  - Injects `ContractRepository` and code generator via custom annotations.
  - Configures file storage directory (`/contract`).
  - Initializes code generation for unique contract codes (e.g., `CTR000001`).
  - Logs operations using SLF4J.

### 7. ContractDto.java
The `ContractDto` is a data transfer object for contract data, extending `AbstractAuditableDto` and implementing `IFileUploadDto`.

- **Key Features**:
  - Fields: `tenant`, `code`, `title`, `description`, `startDate`, `endDate`, `active`, `originalFileName`.
  - Supports file upload via `originalFileName`.

### 8. ContractMapper.java
The `ContractMapper` is a MapStruct interface for mapping between `ContractEntity` and `ContractDto`.

- **Key Features**:
  - Uses Spring component model.
  - Applies null checks with `NullValueCheckStrategy.ALWAYS`.

### 9. FileCrudIntegrationTests.java
The integration tests validate contract and file operations using Testcontainers with a PostgreSQL database. Tests cover CRUD, file upload/download, and edge cases.

- **Key Features**:
  - Uses Testcontainers for PostgreSQL setup.
  - Tests contract creation with and without files.
  - Validates file upload/download and pagination.
  - Ensures data integrity and tenant isolation.

## Setup and Dependencies

- **Spring Boot**: Application framework.
- **JPA/Hibernate**: Database interactions.
- **Testcontainers**: Manages PostgreSQL for testing.
- **MapStruct**: Entity-DTO mapping.
- **Lombok**: Reduces boilerplate code.
- **Jackson**: JSON serialization/deserialization.
- **PostgreSQL**: Database for contract and file data.

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
   The API is available at `http://localhost:8080/api/v1/contract`.

## API Endpoints

- **POST /api/v1/contract**: Create a contract without a file.
- **POST /api/v1/contract/file**: Create a contract with a file (multipart).
- **GET /api/v1/contract**: List all contracts (paged or unpaged).
- **GET /api/v1/contract/{id}**: Retrieve a contract by ID.
- **GET /api/v1/contract/file/download/{id}**: Download a contract’s file.
- **PUT /api/v1/contract/{id}**: Update a contract without a file.
- **PUT /api/v1/contract/file/{id}**: Update a contract with a file (multipart).
- **PUT /api/v1/contract/file/upload/{id}**: Upload a file for an existing contract.
- **DELETE /api/v1/contract/{id}**: Delete a contract.

**Headers**:
- `X-Tenant-ID`: Specifies the tenant (e.g., `tenants`).

## Testing Scenarios

The integration tests cover:
1. Creating contracts with and without files.
2. Updating contracts (with and without files).
3. Uploading and downloading files.
4. Listing contracts with pagination.
5. Deleting contracts.
6. Edge cases: non-existent contracts and file size validation.

## Example Usage

### Create a Contract Without File
```bash
curl -X POST http://localhost:8080/api/v1/contract \
-H "Content-Type: application/json" \
-H "X-Tenant-ID: tenants" \
-d '{
    "tenant": "tenants",
    "code": "CON001",
    "title": "Test Contract",
    "description": "Test contract description",
    "startDate": "2025-07-22T12:00:00",
    "endDate": "2025-10-22T12:00:00",
    "active": true
}'
```

### Create a Contract With File
```bash
curl -X POST http://localhost:8080/api/v1/contract/file \
-H "X-Tenant-ID: tenants" \
-F "dto={\"tenant\":\"tenants\",\"code\":\"CON002\",\"title\":\"Contract with File\",\"description\":\"File upload test\",\"startDate\":\"2025-07-22T12:00:00\",\"endDate\":\"2025-10-22T12:00:00\",\"active\":true};type=application/json" \
-F "file=@test.pdf;type=application/pdf"
```

### Download a Contract’s File
```bash
curl -X GET http://localhost:8080/api/v1/contract/file/download/1?version=0 \
-H "X-Tenant-ID: tenants" \
--output downloaded_contract.pdf
```

### List Contracts (Paginated)
```bash
curl -X GET "http://localhost:8080/api/v1/contract?page=0&size=10" \
-H "X-Tenant-ID: tenants"
```

## Notes

- **Tenant Isolation**: Ensured via `tenant` field and `X-Tenant-ID` header.
- **File Handling**: Files are stored with metadata in `/contract`, supporting PDF format for uploads/downloads.
- **Code Generation**: Unique codes (e.g., `CTR000001`) are generated for contracts.
- **Secondary Table**: File metadata is stored in `CONTRACT_FILE` with tags in `CONTRACT_FILE_TAGS`.
- **Testcontainers**: Eliminates external database dependencies for testing.
- **Extensibility**: The system can be extended for advanced features like file versioning or additional metadata.

For further details, refer to the provided source code files.