# JSON-Embedded Entity Demo

This repository demonstrates the **JSON-Embedded Entity Pattern**, a design for managing tenant-specific data in a
Spring Boot application using JSON storage within a relational database. The demo focuses on managing user login events
in a multitenant environment, showcasing flexible, scalable, and tenant-isolated data handling.

## Table of Contents

- [Overview](#overview)
- [JSON-Embedded Entity Pattern](#json-embedded-multitenancy-pattern)
- [Demo Scope](#demo-scope)
- [Architecture](#architecture)
- [Key Components](#key-components)
- [Setup and Installation](#setup-and-installation)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Technologies Used](#technologies-used)
- [Contributing](#contributing)
- [License](#license)

## Overview

This demo illustrates how to store and manage user login events using a JSON-based approach in a multitenant system.
Each tenant's data is isolated using a `tenant_id`, and event details are stored as JSON in a single database table. The
solution supports CRUD operations, tenant-specific queries, and criteria-based filtering, making it ideal for
applications requiring flexible data structures and tenant isolation.

## JSON-Embedded Entity Pattern

The **JSON-Embedded Entity Pattern** combines the structure of relational databases with the flexibility of JSON data
storage. Key features include:

- **Single Table Storage**: Stores all tenant data in one table (`EVENTS`) with a `tenant_id` for isolation.
- **JSON Attributes**: Entity attributes are serialized as JSON in an `attributes` column, allowing schema flexibility
  without database changes.
- **Generic Service Layer**: A reusable `JsonBasedTenantService` handles tenant-aware CRUD operations, minimizing
  boilerplate code.
- **Extensibility**: Supports new entity types via DTOs and entities without altering the database schema.
- **Tenant Isolation**: Ensures data access is restricted to the authenticated tenant.

This pattern is well-suited for applications needing to manage diverse, tenant-specific data in a single database.

## Demo Scope

The demo focuses on managing **user login events**, capturing details such as user ID, IP address, and device. It
demonstrates:

- Creating, retrieving, updating, and deleting user login events.
- Tenant-specific data isolation.
- JSON serialization/deserialization for flexible data storage.
- RESTful API integration with Spring Boot.

## Architecture

The application follows a layered architecture:

- **Controller Layer**: Exposes REST endpoints for user login event management.
- **Service Layer**: Handles business logic, tenant validation, and JSON processing.
- **Repository Layer**: Uses Spring Data JPA with native SQL queries for JSON data access.
- **Model/DTO Layer**: Defines entities (`EventEntity`, `UserLoginEntity`) and DTOs (`UserLoginEventDto`).
- **Mapper Layer**: Uses MapStruct for entity-DTO conversions.

The `EventEntity` stores tenant-specific data with a JSON `attributes` column containing serialized `UserLoginEntity`
data. The `UserLoginEventTenantService` orchestrates tenant-aware operations using the generic `JsonBasedTenantService`.

## Key Components

- **EventEntity**: Core entity in the `EVENTS` table with `id`, `tenant_id`, `element_type`, and `attributes` (JSON)
  columns.
- **UserLoginEntity**: Represents a user login event, stored as JSON in `EventEntity.attributes`.
- **UserLoginEventDto**: DTO for transferring user login event data.
- **UserLoginEventMapper**: MapStruct mapper for entity-DTO conversions.
- **UserLoginEventTenantService**: Tenant-aware service for CRUD operations.
- **EventTenantAssignableRepository**: JPA repository with tenant-specific JSON queries.
- **UserLoginEventController**: REST controller for API endpoints.

## Setup and Installation

1. **Prerequisites**:
    - Java 17 or higher
    - Maven
    - PostgreSQL (or another database with JSON support)
    - Spring Boot 3.x

2. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-repo/json-embedded-entity-demo.git
   cd json-embedded-entity-demo
   ```

3. **Configure Database**:
    - Update `application.properties` with your database details:
      ```properties
      spring.datasource.url=jdbc:postgresql://localhost:5432/demo_db
      spring.datasource.username=your_username
      spring.datasource.password=your_password
      spring.jpa.hibernate.ddl-auto=update
      ```

4. **Build and Run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access the API**:
    - The API is available at `_http://localhost:8080/api/userlogin_`.
    - Cross-origin requests are allowed from `_http://localhost:8081_`.

## Usage

The demo provides a REST API to manage user login events. Each request must include a tenant identifier (e.g., via
`X-Tenant-ID` header) for data isolation.

### Example Request

**Create a User Login Event**:

```bash
curl -X POST http://localhost:8080/api/userlogin \
-H "Content-Type: application/json" \
-H "X-Tenant-ID: tenant1" \
-d '{
  "userId": "user123",
  "ip": "192.168.1.1",
  "device": "Chrome/120.0"
}'
```

**Response**:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user123",
  "ip": "192.168.1.1",
  "device": "Chrome/120.0"
}
```

## API Endpoints

- **POST /api/userlogin**: Create a new user login event (for a tenant).
- **GET /api/userlogin/{id}**: Retrieve a user login event by ID (for a tenant).
- **GET /api/userlogin** : Retrieve all user login events (for a tenant).

**: List all user login events (for a tenant) (supports pagination).

- **PUT /api/userlogin**: Update an existing user login event (for a tenant).
- **DELETE /api/userlogin/{id}**: Delete a user login event by ID (for a tenant).
- **POST /api/userlogin/batch**: Create or update multiple user login events.
- **DELETE /api/userlogin/batch**: Delete multiple user login events.

**For multitenancy mode, All endpoints require a tenant identifier via the `X-Tenant-ID` header.**

## Technologies Used

- **Spring Boot**: For building the REST API.
- **Spring Data JPA**: For database operations and repository management.
- **MapStruct**: For entity-DTO mapping.
- **Jackson**: For JSON serialization/deserialization.
- **PostgreSQL**: Database with JSONB support for storing event attributes.
- **Lombok**: To reduce boilerplate code.

## Contributing

Contributions are welcome! To contribute:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m 'Add your feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.