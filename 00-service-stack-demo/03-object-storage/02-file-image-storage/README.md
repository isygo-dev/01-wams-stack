# Multitenancy CRUD Implementations with Reusable Generic Components

This repository provides a suite of Spring Boot implementations showcasing CRUD operations with multitenancy support,
designed for rapid development through reusable generic classes and interfaces. Each example focuses on a specific
entity type with varying attachment capabilities (basic entity, entity with image, entity with file, and entity with
both image and file). By leveraging generic abstractions like `MappedCrudTenantController`,
`MappedFileTenantController`, `MappedImageTenantController`, `IFileEntity`, `IImageEntity`, and `ICodeAssignable`, these
implementations reduce development time from hours to minutes, enabling developers to quickly scaffold robust,
tenant-aware applications.

## Reusability Through Generic Classes and Interfaces

The implementations utilize a highly reusable architecture that abstracts common CRUD and attachment-handling logic into
generic classes and interfaces, minimizing boilerplate code and ensuring consistency across entities. Key components
include:

- **MappedCrudTenantController**: A generic base controller providing tenant-aware CRUD endpoints (create, read, update,
  delete). It handles HTTP requests, tenant isolation via the `X-Tenant-ID` header, and integrates with services and
  mappers. Developers can extend this controller for any entity with minimal configuration, typically requiring only
  annotations to inject the appropriate service and mapper.

- **MappedFileTenantController**: Extends the CRUD controller to add file-specific endpoints (e.g., upload and
  download). It works with entities implementing `IFileEntity`, streamlining file handling across different entity
  types.

- **MappedImageTenantController**: Similar to `MappedFileTenantController`, but tailored for image-specific operations,
  working seamlessly with entities implementing `IImageEntity`.

- **IFileEntity**: An interface defining file-related fields (e.g., `fileName`, `originalFileName`, `path`, `extension`,
  `type`, `tags`) and methods. Entities implementing this interface automatically gain file-handling capabilities, with
  metadata stored in a secondary table.

- **IImageEntity**: An interface for image-related fields (e.g., `imagePath`) and methods. Entities implementing this
  interface support image upload and download, integrated with the storage logic in the service layer.

- **ICodeAssignable**: An interface for entities requiring unique code generation (e.g., `RES000001`, `CTR000001`). It
  integrates with a code generation service to ensure consistent identifier creation across entities.

- **FileImageTenantService**: A generic service combining file and image handling with tenant-aware operations. It
  abstracts business logic for entities supporting both `IFileEntity` and `IImageEntity`, reducing the need for custom
  service code.

These generic components are designed to work with any entity, repository, and DTO, requiring only minimal
configuration (e.g., specifying the entity class, repository, and mapper). This approach eliminates repetitive coding
tasks, such as defining standard CRUD endpoints or file/image handling logic, allowing developers to focus on
entity-specific requirements. By extending these classes and implementing the interfaces, a fully functional,
tenant-aware CRUD system with attachment support can be implemented in minutes, compared to hours for a traditional,
non-reusable approach.

## Examples

Below are links to the detailed documentation for each example, showcasing how the generic components are applied to
different entity types:

- [Simple CRUD Implementation](Simple_CRUD_Implementation.md): Demonstrates a basic CRUD system for an `Account` entity
  using `MappedCrudTenantController` and `ICodeAssignable`. It focuses on core operations without attachments,
  highlighting the rapid setup of tenant-aware endpoints.

- [Entity with Image Attached](Entity_With_Image_Attached.md): Implements a `User` entity with image support using
  `MappedImageTenantController` and `IImageEntity`. It showcases how the generic image-handling logic reduces
  development time for image uploads and downloads.

- [Entity with File Attached](Entity_With_File_Attached.md): Features a `Contract` entity with file support (e.g., PDFs)
  using `MappedFileTenantController` and `IFileEntity`. It demonstrates the ease of adding file-handling capabilities
  with minimal code.

- [Entity with Image and File Attached](Entity_With_Image_And_File_Attached.md): Provides a comprehensive implementation
  for a `Resume` entity supporting both images and files using `MappedFileTenantController`,
  `MappedImageTenantController`, `IFileEntity`, and `IImageEntity`. It illustrates the power of combining multiple
  generic components for complex use cases.

## Benefits of the Approach

- **Rapid Development**: By reusing generic controllers, services, and interfaces, developers can scaffold a complete
  CRUD system with attachment support in minutes. For example, defining a new entity requires only creating the entity
  class, DTO, and mapper, then extending the appropriate generic controller and service.

- **Consistency**: The use of standardized interfaces and base classes ensures consistent behavior across entities, such
  as uniform tenant isolation, error handling, and API design.

- **Scalability**: The generic components are designed to handle multitenancy, pagination, and concurrent operations,
  making them suitable for large-scale applications.

- **Testability**: Integration tests, powered by Testcontainers, validate all CRUD and attachment operations, ensuring
  reliability. The reusable test structure further reduces setup time for new entities.

- **Extensibility**: Developers can easily add custom logic or extend functionality (e.g., advanced filtering, file
  versioning) without modifying the core generic components.

## Implementation Details

Each example uses:

- **Spring Boot**: For the application framework.
- **JPA/Hibernate**: For database interactions.
- **Testcontainers**: For PostgreSQL testing, eliminating external database dependencies.
- **MapStruct**: For entity-DTO mapping, integrated with generic mappers.
- **Lombok**: To reduce boilerplate code.
- **Jackson**: For JSON serialization/deserialization.
- **PostgreSQL**: For data persistence with tenant-specific schemas.

The implementations enforce tenant isolation using the `X-Tenant-ID` header and support features like code generation (
via `ICodeAssignable`), pagination, and file/image storage in designated directories (e.g., `/resume`, `/contract`).
Integration tests cover CRUD operations, attachment handling, concurrent operations, and edge cases, ensuring robust
functionality.

## Getting Started

To explore these implementations:

1. **Clone the Repository** (if applicable).
2. **Install Dependencies**:
   ```bash
   mvn clean install
   ```
3. **Run Tests**:
   ```bash
   mvn test
   ```
   Testcontainers spins up a PostgreSQL container for testing.
4. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```
   APIs are available at `http://localhost:8080/api/v1/<entity>`.

For detailed setup instructions, API endpoints, and example usage, refer to the individual documentation files linked
above. Each example demonstrates how the generic classes and interfaces streamline development, enabling a fully
functional, tenant-aware system with attachment support to be built in minutes.