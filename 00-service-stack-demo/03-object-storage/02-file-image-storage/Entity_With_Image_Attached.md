# Entity with Image Attached

This document describes a Spring Boot implementation for managing entities with attached images in a multitenant environment. The example focuses on a `User` entity with support for CRUD operations and image handling (upload and download). It uses Spring Boot, JPA, Testcontainers for integration testing, and MapStruct for entity-DTO mapping. The system ensures tenant isolation and includes comprehensive integration tests.

## Overview

The implementation provides a RESTful API for managing user entities, including the ability to attach and retrieve images. It leverages Spring Boot for the backend, PostgreSQL (via Testcontainers) for data persistence, and MapStruct for mapping between entities and DTOs. The system supports multitenancy and includes tests for CRUD operations, image handling, concurrent operations, and edge cases.

## Components

### 1. UserController.java
The `UserController` is a REST controller handling standard CRUD operations for users. It extends `MappedCrudTenantController` to provide tenant-aware endpoints.

- **Key Features**:
  - Manages user creation, retrieval, update, and deletion.
  - Enforces tenant isolation using the `X-Tenant-ID` header.
  - Uses `@CrossOrigin` for local development (CORS).
  - Injects `UserMapper` and `UserService` via custom annotations.

### 2. UserImageController.java
The `UserImageController` extends `MappedImageTenantController` to handle image-specific operations (upload and download) for users.

- **Key Features**:
  - Supports image upload and download endpoints.
  - Integrates with `UserService` for image storage and retrieval.
  - Maintains tenant isolation.

### 3. UserEntity.java
The `UserEntity` represents the user table (`USER_DETAILS`) in the database. It extends `AuditableEntity` and implements `ITenantAssignable`, `IImageEntity`, and `ICodeAssignable`.

- **Key Features**:
  - Fields: `id`, `tenant`, `code`, `firstName`, `lastName`, `active`, `imagePath`.
  - `@Id` with sequence-based ID generation.
  - `@Criteria` on `firstName` and `lastName` for filtering.
  - Supports image storage via `imagePath`.
  - Implements code generation for unique identifiers.

### 4. UserRepository.java
The `UserRepository` is a JPA repository extending `JpaPagingAndSortingTenantAndCodeAssignableRepository` to support tenant-aware and code-aware queries with pagination and sorting.

- **Key Features**:
  - Provides CRUD operations for `UserEntity`.
  - Supports tenant and code-based filtering.

### 5. UserService.java
The `UserService` handles business logic for user and image operations. It extends `ImageTenantService` and uses `UserRepository` for persistence.

- **Key Features**:
  - Transactional operations with `@Transactional`.
  - Injects `UserRepository` and code generator via custom annotations.
  - Configures image storage directory (`/user/avatar`).
  - Initializes code generation for unique user codes (e.g., `USR000001`).
  - Logs operations using SLF4J.

### 6. UserDto.java
The `UserDto` is a data transfer object for user data, extending `AbstractAuditableDto` and implementing `IImageUploadDto`.

- **Key Features**:
  - Fields: `tenant`, `firstName`, `lastName`, `active`, `imagePath`.
  - Validation: `@NotNull` for required fields.
  - Supports image upload via `imagePath`.

### 7. UserMapper.java
The `UserMapper` is a MapStruct interface for mapping between `UserEntity` and `UserDto`.

- **Key Features**:
  - Uses Spring component model.
  - Applies null checks with `NullValueCheckStrategy.ALWAYS`.

### 8. ImageCrudIntegrationTests.java
The integration tests validate user and image operations using Testcontainers with a PostgreSQL database. Tests cover CRUD, image upload/download, concurrent operations, and edge cases.

- **Key Features**:
  - Uses Testcontainers for PostgreSQL setup.
  - Tests user creation with and without images.
  - Validates image upload/download, pagination, and concurrent operations.
  - Checks edge cases like invalid DTOs, oversized fields, and missing tenant headers.
  - Ensures data integrity and tenant isolation.

## Setup and Dependencies

- **Spring Boot**: Application framework.
- **JPA/Hibernate**: Database interactions.
- **Testcontainers**: Manages PostgreSQL for testing.
- **MapStruct**: Entity-DTO mapping.
- **Lombok**: Reduces boilerplate code.
- **Jackson**: JSON serialization/deserialization.
- **PostgreSQL**: Database for user and image data.

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
   The API is available at `http://localhost:8080/api/v1/user`.

## API Endpoints

- **POST /api/v1/user**: Create a user without an image.
- **POST /api/v1/user/image**: Create a user with an image (multipart).
- **POST /api/v1/user/batch**: Create multiple users.
- **GET /api/v1/user**: List all users (paged or unpaged).
- **GET /api/v1/user/{id}**: Retrieve a user by ID.
- **GET /api/v1/user/image/download/{id}**: Download a user’s image.
- **PUT /api/v1/user/{id}**: Update a user without an image.
- **PUT /api/v1/user/image/{id}**: Update a user with an image (multipart).
- **PUT /api/v1/user/image/upload/{id}**: Upload an image for an existing user.
- **DELETE /api/v1/user/{id}**: Delete a user.
- **DELETE /api/v1/user/batch**: Delete multiple users.

**Headers**:
- `X-Tenant-ID`: Specifies the tenant (e.g., `tenants`).

## Testing Scenarios

The integration tests cover:
1. Creating users with and without images.
2. Updating users (with and without images).
3. Uploading and downloading images.
4. Listing users with pagination.
5. Concurrent operations (creation, updates, downloads).
6. Batch creation and deletion.
7. Edge cases: invalid DTOs, oversized fields, invalid image formats, missing tenant headers, and non-existent users.

## Example Usage

### Create a User Without Image
```bash
curl -X POST http://localhost:8080/api/v1/user \
-H "Content-Type: application/json" \
-H "X-Tenant-ID: tenants" \
-d '{
    "tenant": "tenants",
    "firstName": "John",
    "lastName": "Doe",
    "active": true,
    "imagePath": ""
}'
```

### Create a User With Image
```bash
curl -X POST http://localhost:8080/api/v1/user/image \
-H "X-Tenant-ID: tenants" \
-F "dto={\"tenant\":\"tenants\",\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"active\":true,\"imagePath\":\"\"};type=application/json" \
-F "file=@test.png;type=image/png"
```

### Download a User’s Image
```bash
curl -X GET http://localhost:8080/api/v1/user/image/download/1 \
-H "X-Tenant-ID: tenants" \
--output downloaded_image.png
```

### List Users (Paginated)
```bash
curl -X GET "http://localhost:8080/api/v1/user?page=0&size=2" \
-H "X-Tenant-ID: tenants"
```

## Notes

- **Tenant Isolation**: Ensured via `tenant` field and `X-Tenant-ID` header.
- **Image Handling**: Images are stored with paths in `/user/avatar`, and the system supports PNG format for uploads/downloads.
- **Code Generation**: Unique codes (e.g., `USR000001`) are generated for users.
- **Testcontainers**: Eliminates external database dependencies for testing.
- **Extensibility**: The system can be extended for additional features like advanced filtering or authentication.

For further details, refer to the provided source code files.