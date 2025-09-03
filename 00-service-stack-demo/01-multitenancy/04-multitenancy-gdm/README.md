# multitenancy Discriminator Tenant PoC

This repository contains a Proof of Concept (PoC) for a multi-tenant Spring Boot application using a discriminator-based
tenant strategy. The application demonstrates tenant-aware CRUD operations for a `Tutorial` entity, leveraging Spring
Data JPA, Hibernate, and MapStruct. It supports both H2 and PostgreSQL databases with tenant-specific schema
initialization.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Technologies](#technologies)
- [Setup Instructions](#setup-instructions)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Database Configuration](#database-configuration)
- [Usage](#usage)
    - [Running the Application](#running-the-application)
    - [API Endpoints](#api-endpoints)
- [Tenant Management](#tenant-management)
- [Contributing](#contributing)
- [License](#license)

## Overview

The multitenancy Discriminator Tenant PoC is designed to showcase a scalable, tenant-aware architecture where each
tenant's data is isolated using a tenant identifier column (`TENANT_ID`) in the database. The `Tutorial` entity serves
as the primary example, with CRUD operations managed through a REST API. The application supports dynamic schema
initialization for tenants and integrates with both H2 (for development) and PostgreSQL (for production-like
environments).

## Features

- **multitenancy**: Supports tenant isolation using a discriminator column (`TENANT_ID`).
- **CRUD Operations**: Full Create, Read, Update, Delete functionality for the `Tutorial` entity.
- **Database Support**: Configurable for H2 (in-memory) and PostgreSQL databases.
- **Tenant Schema Initialization**: Automatically creates tenant-specific schemas using SQL scripts.
- **REST API**: Exposes tenant-aware endpoints for managing tutorials.
- **Swagger Documentation**: Integrated OpenAPI documentation for API exploration.
- **Mapper Integration**: Uses MapStruct for entity-DTO mapping.
- **Validation**: Includes tenant validation and error handling.

## Project Structure

```
multitenancy-poc/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── eu.isygoit.multitenancy/
│   │   │   │   ├── controller/        # REST controllers (TutorialController)
│   │   │   │   ├── dto/              # Data Transfer Objects (TutorialDto)
│   │   │   │   ├── mapper/           # MapStruct mappers (TutorialMapper)
│   │   │   │   ├── model/            # JPA entities (Tutorial)
│   │   │   │   ├── repository/       # Spring Data JPA repositories (TutorialRepository)
│   │   │   │   ├── service/          # Business logic (TutorialService, TenantValidator)
│   │   │   │   ├── utils/            # Utility classes for tenant schema initialization
│   │   │   │   └── MultiTenancyApplication.java  # Spring Boot application entry point
│   │   └── resources/
│   │       ├── db/
│   │       │   ├── h2_tenant-schema.sql  # H2 schema initialization script
│   │       │   └── pg_tenant-schema.sql  # PostgreSQL schema initialization script
│   │       └── application.properties    # Application configuration
├── pom.xml                               # Maven build file
└── README.md                             # This file
```

## Technologies

- **Java 17**: Core programming language.
- **Spring Boot 3.x**: Framework for building the application.
- **Spring Data JPA**: For database operations.
- **Hibernate**: Multi-tenant support with discriminator strategy.
- **H2 Database**: In-memory database for development.
- **PostgreSQL**: Production-grade database support.
- **MapStruct**: For entity-DTO mapping.
- **Lombok**: Reduces boilerplate code.
- **Swagger/OpenAPI**: API documentation.
- **Maven**: Build tool.

## Setup Instructions

### Prerequisites

- **Java 17**: Ensure JDK 17 is installed.
- **Maven**: For building the project.
- **Database**:
    - H2 (included for development).
    - PostgreSQL (optional, for production-like setup).
- **IDE**: IntelliJ IDEA, Eclipse, or similar (recommended).
- **Git**: To clone the repository.

### Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/multitenancy-poc.git
   cd multitenancy-poc
   ```

2. **Build the Project**:
   ```bash
   mvn clean install
   ```

3. **Configure Application Properties**:
    - Edit `src/main/resources/application.properties` to set up database configurations:
      ```properties
      # For H2 (development)
      spring.profiles.active=h2
      spring.datasource.url=jdbc:h2:mem:testdb
      spring.datasource.driverClassName=org.h2.Driver
      spring.datasource.username=sa
      spring.datasource.password=
      spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
 
      # For PostgreSQL (production)
      # spring.profiles.active=postgres
      # spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
      # spring.datasource.username=your-username
      # spring.datasource.password=your-password
      # spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
      ```

4. **Database Configuration**:
    - For **H2**, no additional setup is needed; the in-memory database is created automatically.
    - For **PostgreSQL**, ensure the database server is running and create a database named `postgres` (or update the
      URL in `application.properties`).
    - Tenant schemas are initialized automatically via `H2TenantService` or `PGTenantService` using SQL scripts in
      `src/main/resources/db/`.

### Database Configuration

- **H2**: Uses `h2_tenant-schema.sql` to initialize tenant schemas. The default schema is `public`.
- **PostgreSQL**: Uses `pg_tenant-schema.sql` to create tenant-specific schemas (e.g., `tenant1`, `tenant2`).
- **Tenant Validation**: The `TenantValidator` class defines valid tenants (`tenant1`, `tenant2`, `public`,
  `super-tenant`). Update the `validTenants` set in `TenantValidator.java` to add more tenants.

## Usage

### Running the Application

1. **Run with Maven**:
   ```bash
   mvn spring-boot:run
   ```

2. **Access the Application**:
    - The application runs on `http://localhost:8081` by default.
    - Swagger UI is available at `http://localhost:8081/swagger-ui/index.html` for API documentation.

### API Endpoints

The `TutorialController` exposes REST endpoints under `/api/tutorials`. Key endpoints include:

- **GET /api/tutorials**: Retrieve all tutorials for the authenticated tenant.
- **GET /api/tutorials/{id}**: Retrieve a tutorial by ID.
- **POST /api/tutorials**: Create a new tutorial.
- **PUT /api/tutorials/{id}**: Update an existing tutorial.
- **DELETE /api/tutorials/{id}**: Delete a tutorial.
- **GET /api/tutorials?page={page}&size={size}**: Retrieve paginated tutorials.
- **GET /api/tutorials?criteria={criteria}**: Retrieve tutorials filtered by criteria (e.g., `title=example`).

**Example Request** (Create a Tutorial):

```bash
curl -X POST http://localhost:8081/api/tutorials \
-H "Content-Type: application/json" \
-H "X-Tenant-Id: tenant1" \
-d '{"title":"Sample Tutorial","description":"A sample tutorial","published":true}'
```

**Note**: Include the `X-Tenant-Id` header with a valid tenant ID (e.g., `tenant1`, `tenant2`, `public`,
`super-tenant`).

## Tenant Management

- **Tenant Initialization**: The `H2TenantService` or `PGTenantService` initializes tenant schemas on demand using SQL
  scripts. Add new tenants by updating the `validTenants` set in `TenantValidator.java` and ensuring the corresponding
  schema is initialized.
- **Super Tenant**: The `super-tenant` has access to all tenant data and is used for administrative operations.
- **Schema Strategy**: Uses a discriminator column (`TENANT_ID`) for tenant isolation, with separate schemas for
  PostgreSQL.

## Contributing

Contributions are welcome! To contribute:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a Pull Request.

Please ensure code follows the existing style, includes tests, and updates documentation as needed.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.