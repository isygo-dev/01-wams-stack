# Simple CRUD Implementation

This document provides an overview of a simple CRUD (Create, Read, Update, Delete) implementation for a multitenant
account management system using Spring Boot, JPA, and Testcontainers for integration testing. The example includes key
components such as the controller, entity, repository, service, DTO, mapper, and integration tests.

## Overview

The implementation demonstrates a RESTful API for managing accounts in a multitenant environment. It uses Spring Boot
for the backend, PostgreSQL as the database (managed via Testcontainers for testing), and MapStruct for mapping between
entities and DTOs. The system enforces tenant isolation and includes comprehensive integration tests to validate CRUD
operations, edge cases, and data integrity.

## Components

### 1. AccountController.java

The `AccountController` is a REST controller that handles HTTP requests for account-related operations. It extends
`MappedCrudTenantController` to provide standard CRUD endpoints with tenant awareness.

- **Key Features**:
    - Handles CRUD operations for accounts.
    - Supports tenant isolation via the `X-Tenant-ID` header.
    - Uses `@CrossOrigin` for local development (CORS).
    - Injects `AccountMapper` and `AccountService` using custom annotations.

### 2. AccountEntity.java

The `AccountEntity` represents the account table in the database. It extends `AuditableEntity` for audit fields and
implements `ITenantAssignable` for multitenancy.

- **Key Features**:
    - Fields: `id`, `tenant`, `login`, `email`, `passkey`.
    - `@Id` with sequence-based ID generation.
    - `@Criteria` annotation on `login` for filtering.
    - Enforces constraints like non-nullable fields and unique login.

### 3. AccountRepository.java

The `AccountRepository` is a JPA repository interface extending `JpaPagingAndSortingTenantAssignableRepository` to
support tenant-aware queries with pagination and sorting.

- **Key Features**:
    - Provides default CRUD operations.
    - Supports tenant-specific data retrieval.

### 4. AccountService.java

The `AccountService` handles business logic for account operations. It extends `CrudTenantService` and uses the
`AccountRepository` for database interactions.

- **Key Features**:
    - Transactional operations with `@Transactional`.
    - Injects `AccountRepository` using a custom annotation.
    - Logs operations using SLF4J.

### 5. AccountDto.java

The `AccountDto` is a data transfer object for transferring account data between layers. It extends
`AbstractAuditableDto` and includes validation annotations.

- **Key Features**:
    - Fields: `tenant`, `login`, `email`, `passkey`.
    - Validation: `@NotNull` and `@Email` for data integrity.

### 6. AccountMapper.java

The `AccountMapper` is a MapStruct interface for mapping between `AccountEntity` and `AccountDto`.

- **Key Features**:
    - Uses Spring component model.
    - Handles null checks with `NullValueCheckStrategy.ALWAYS`.

### 7. SimpleCrudIntegrationTests.java

The integration tests validate the CRUD operations using Testcontainers to spin up a PostgreSQL database. The tests
cover various scenarios, including concurrent operations and filtering.

- **Key Features**:
    - Uses Testcontainers to manage a PostgreSQL instance.
    - Tests CRUD operations, edge cases (e.g., invalid email, duplicate login), and pagination.
    - Simulates concurrent account creation with multiple threads.
    - Validates tenant isolation and data integrity.

## Setup and Dependencies

- **Spring Boot**: Provides the application framework.
- **JPA/Hibernate**: Handles database interactions.
- **Testcontainers**: Manages PostgreSQL for testing.
- **MapStruct**: Maps entities to DTOs.
- **Lombok**: Reduces boilerplate code (e.g., getters, setters).
- **Jackson**: Handles JSON serialization/deserialization.
- **PostgreSQL**: Database for storing account data.

### Prerequisites

- Java 17 or later.
- Maven for dependency management.
- Docker for Testcontainers.

### Configuration

- **Database**: PostgreSQL 15 (configured via Testcontainers).
- **Multitenancy**: Uses GDM mode with tenant-specific schemas.
- **Properties**:
    - `spring.jpa.hibernate.ddl-auto=update`
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
   The tests use Testcontainers to start a PostgreSQL container and execute the integration tests.

4. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```
   The API will be available at `http://localhost:8080/api/v1/account`.

## API Endpoints

- **POST /api/v1/account**: Create a new account.
- **POST /api/v1/account/batch**: Create multiple accounts.
- **GET /api/v1/account**: Retrieve all accounts (paged or unpaged).
- **GET /api/v1/account/full**: Retrieve all accounts with full details.
- **GET /api/v1/account/{id}**: Retrieve an account by ID.
- **GET /api/v1/account/count**: Get the total number of accounts.
- **GET /api/v1/account/filter**: Retrieve accounts with filtering criteria.
- **PUT /api/v1/account/{id}**: Update an account.
- **DELETE /api/v1/account/{id}**: Delete an account.

**Headers**:

- `X-Tenant-ID`: Specifies the tenant (e.g., `tenants`).

## Testing Scenarios

The integration tests cover:

1. Creating accounts with valid and invalid data.
2. Handling duplicate logins.
3. Concurrent account creation.
4. Pagination and filtering.
5. Updating and deleting accounts.
6. Edge cases like non-existent IDs and missing tenant headers.

## Example Usage

### Create an Account

```bash
curl -X POST http://localhost:8080/api/v1/account \
-H "Content-Type: application/json" \
-H "X-Tenant-ID: tenants" \
-d '{
    "tenant": "tenants",
    "login": "testuser",
    "email": "testuser@example.com",
    "passkey": "securepass123"
}'
```

### Retrieve All Accounts

```bash
curl -X GET http://localhost:8080/api/v1/account \
-H "X-Tenant-ID: tenants"
```

### Filter Accounts

```bash
curl -X GET "http://localhost:8080/api/v1/account/filter?criteria=login='testuser'" \
-H "X-Tenant-ID: tenants"
```

## Notes

- The implementation ensures tenant isolation by including the `tenant` field in entities and using the `X-Tenant-ID`
  header.
- The tests use a PostgreSQL container to avoid dependencies on an external database.
- The system is designed to be extensible for additional features like advanced filtering or authentication.

For further details, refer to the source code files provided.