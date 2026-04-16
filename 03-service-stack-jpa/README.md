# Service Stack - JPA

**Version**: `WS-1.0.260408-T1636`

A common library for the JPA implementation layer in Spring Boot microservices. It provides a set of base entities, repository interfaces, auditing utilities, and a dynamic query helper to standardize and accelerate development.

## 1. General Description & Usability

The `service-stack-jpa` module is a core part of the ISYGO-IT service stack, designed to streamline the data access layer in modern Spring Boot microservices. Its primary goal is to enforce a consistent domain model and repository pattern across all services in a microservice ecosystem.

### Key Usability Benefits:
- **Standardization**: Ensures all microservices use the same auditing, multi-tenancy, and soft-delete patterns.
- **Productivity**: Reduces boilerplate code by providing powerful base classes and generic repositories.
- **Dynamic Filtering**: Allows frontend applications to perform complex searches using a simple SQL-like syntax without additional backend development.
- **Security**: Built-in support for row-level multi-tenancy and automatic auditing of data modifications.

## 2. Technologies & Versions

The library is built on a modern stack to ensure performance, security, and compatibility with the latest Spring ecosystem:

| Technology | Version | Description |
| :--- | :--- | :--- |
| **Java** | 17+ | Long-Term Support (LTS) version |
| **Spring Boot** | 3.x | Managed through parent dependency |
| **Spring Data JPA** | 3.x | Modern data access abstraction |
| **Jakarta Persistence** | 3.x | Standardized JPA API |
| **Hibernate** | 6.x | Default persistence provider |
| **Lombok** | Latest | Boilerplate reduction |
| **Apache Commons** | Text/IO | Text processing for dynamic queries |

## 3. Features & Class Usability

This section explains the usability and purpose of each class type provided by the library.

### 3.1. Base Entities
The library provides several base classes for entities using Jakarta Persistence:
- **`AbstractEntity<I>`**:
  - *Usability*: The root class for any entity. It provides a generic ID field and implements `IIdAssignable`. Use this for simple lookup tables or entities that don't require auditing.
- **`AuditableEntity<I>`**:
  - *Usability*: Extends `AbstractEntity` with fields for `createDate`, `createdBy`, `updateDate`, and `updatedBy`. Use this for entities where tracking the history of changes is important (e.g., UserProfiles, Settings).
- **`CancelableEntity<I>`**:
  - *Usability*: Extends `AbstractEntity` with fields for soft-delete support (`checkCancel`, `cancelDate`, `canceledBy`). Use this for data that should never be physically deleted from the database but only marked as inactive (e.g., Orders, Subscriptions).
- **`AuditableCancelableEntity<I>`**:
  - *Usability*: Combines auditing and soft-delete. This is the most common base class for business-critical entities.
- **`AuditableTenantEntity<I>`**:
  - *Usability*: Extends `AuditableEntity` and implements `ITenantAssignable`. Crucial for SaaS applications to ensure data isolation between different customers (tenants). It includes a standardized `tenant` field mapped to `tenant_id`.

All base entities use modern `java.time.LocalDateTime` for timestamps. `CancelableEntity` and its subclasses automatically filter out canceled records from standard queries using Hibernate's `@SQLRestriction`.

### 3.2. Standardized Repositories
A hierarchy of repository interfaces extending `JpaRepository` and `JpaSpecificationExecutor`:
- **`JpaPagingAndSortingRepository`**: Adds `findByIdIn(List<I> ids)` and `findActiveById(I id)` support. Use as the default repository for standard entities. `findActiveById` is particularly useful for soft-delete aware lookups.
- **`JpaPagingAndSortingCodeAssignableRepository`**: For entities with a unique `code` field. Simplifies lookups by human-readable identifiers.
- **`JpaPagingAndSortingTenantAssignableRepository`**: For entities belonging to a tenant. Automatically handles tenant-aware queries.
- **`JpaPagingAndSortingTenantAndCodeAssignableRepository`**: Combines tenant and code support.
- **`JsonBasedRepository`**: Specialized repository for entities with JSONB/JSON attributes (PostgreSQL). Useful for metadata-driven or highly flexible data structures.

### 3.3. Dynamic Query Support (`CriteriaHelper`)
The `CriteriaHelper` utility allows building complex JPA Specifications from a simple SQL-like string.

- **Purpose**: To provide a flexible and powerful way for clients to filter data without requiring custom repository methods or complex backend logic for each search requirement.
- **Usability**:
    - **Annotation-Driven**: Only fields marked with `@Criteria` are searchable, providing a layer of security and control over what can be queried.
    - **Inheritance Support**: It automatically scans the entire class hierarchy for `@Criteria` annotations.
    - **Case-Insensitive**: String-based searches (`LIKE`, `NOT LIKE`) are case-insensitive by default.
    - **Performance**: Metadata about criteria fields is cached to ensure that reflection overhead is minimized.

### 3.4. Criteria and Query Syntax Usability
The library supports a simplified SQL-like `WHERE` clause syntax that is parsed into JPA Specifications.

#### 3.4.1. Supported Operators
| Operator | Symbol | Meaning | Example |
| :--- | :--- | :--- | :--- |
| **Equal** | `=` | Exact match | `status = 'ACTIVE'` |
| **Not Equal** | `!=` | Does not match | `type != 'INTERNAL'` |
| **Like** | `~` | Case-insensitive partial match | `name ~ 'john'` |
| **Not Like** | `!~` | Case-insensitive negative partial match | `name !~ 'doe'` |
| **Greater Than** | `>` | Strictly greater than | `age > 21` |
| **Greater or Equal** | `>=` | Greater than or equal to | `salary >= 5000` |
| **Less Than** | `<` | Strictly less than | `createdDate < '2023-01-01'` |
| **Less or Equal** | `<=` | Less than or equal to | `priority <= 2` |
| **In** | `IN` | Matches any value in list | `status IN ('ACTIVE', 'PENDING')` |
| **Between** | `BW` | Value within range | `age BW (18, 65)` |

#### 3.4.2. Logical Combiners and Grouping
- **AND (`&`)**: All conditions must be met.
- **OR (`|`)**: At least one condition must be met.
- **Parentheses `()`**: Used to group conditions and control evaluation order.

#### 3.4.3. Complex Query Example
`name ~ 'John' & (status = 'ACTIVE' | role = 'ADMIN')`
This query will find all entities where the name contains "John" (case-insensitive) AND either the status is "ACTIVE" OR the role is "ADMIN".

### 3.5. JPA Auditing
Automatic auditing configuration is provided:
- **`IAuditorAwareService`**: Interface for resolving the current user (auditor).
- **`AuditorAwareImpl`**: Bridge between Spring Data Auditing and the security context.
- **`JpaConfig`**: Automatically enables JPA auditing if a bean of `IAuditorAwareService` is present.

## 4. Getting Started

### Prerequisites
- Java 17+
- Spring Boot 3.x
- Maven

### Dependency
Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>eu.isygo-it.services</groupId>
    <artifactId>service-stack-jpa</artifactId>
    <version>${project.version}</version>
</dependency>
```

### Usage Example

#### 1. Define an Entity
```java
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "my_entity")
public class MyEntity extends AuditableCancelableEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Criteria // Mark field for use in dynamic queries
    @Column(name = "name")
    private String name;
}
```

#### 2. Define a Repository
```java
public interface MyEntityRepository extends JpaPagingAndSortingRepository<MyEntity, Long> {
}
```

#### 3. Use in Service
```java
@Service
public class MyService {
    @Autowired
    private MyEntityRepository repository;

    public Page<MyEntity> search(String query, Pageable pageable) {
        List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(query);
        Specification<MyEntity> spec = CriteriaHelper.buildSpecification(null, criteria, MyEntity.class);
        return repository.findAll(spec, pageable);
    }
}
```

## Maintenance and Design
The module is designed for:
- **Type Safety**: Heavy use of Generics to prevent runtime errors.
- **Performance**: Metadata caching in `CriteriaHelper`.
- **Readability**: Consistent use of Lombok and clean interface hierarchies.
- **Security**: Up-to-date dependencies and standardized auditing.
