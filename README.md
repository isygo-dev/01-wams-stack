# 01-wams-stack

> Isygo-IT WAMS backend microservices stack — a collection of reusable, production-grade
> library modules and proof-of-concept applications for building multi-tenant,
> event-driven microservices on the Spring Boot 3.x platform.

---

## Technology Stack

| Component              | Version     |
|------------------------|-------------|
| Java                   | 17+         |
| Spring Boot            | 3.5.11      |
| Spring Cloud           | 2025.0.1    |
| Spring Framework       | 6.2.x       |
| Spring Security        | 6.5.x       |
| Spring Data JPA        | 2025.0.x    |
| Spring Data Cassandra  | 5.x         |
| Hibernate ORM          | 6.6.x       |
| PostgreSQL Driver      | 42.7.x      |
| HikariCP               | 6.x         |
| Apache Camel           | 4.14.0      |
| MapStruct              | 1.6.3       |
| Lombok                 | 1.18.36     |
| JJWT                   | 0.12.6      |
| SpringDoc OpenAPI      | 2.8.8       |
| Flyway                 | 11.x        |
| Testcontainers         | 1.20.6      |
| Maven                  | 3.9+        |
| Docker                 | Latest      |

> All Spring-managed versions (Spring Framework, Security, Data, Hibernate, HikariCP,
> PostgreSQL, Flyway, JUnit 5, Mockito) are governed by the Spring Boot 3.5.11 BOM.

---

## Project Structure

```
01-wams-stack/
├── 01-service-stack-parent/        # Spring Boot BOM parent for all microservices
├── 02-service-stack-shared/        # Foundation: Jackson, Camel, Feign, OpenAPI, validation
├── 03-service-stack-jpa/           # ORM: Hibernate 6, Spring Data JPA, auditing
├── 04-service-stack-crypt/         # Security: JJWT 0.12, BCrypt, Jasypt encryption
├── 05-service-stack-web/           # Web layer: Spring Security, Kafka, Micrometer
├── 06-service-stack-quartz/        # Distributed job scheduling via Quartz
├── 07-service-stack-cassandra/     # Spring Data Cassandra integration
├── 08-service-stack-multitenancy/  # Multi-tenancy: discriminator, schema, database, GDM
├── 09-service-stack-storage/       # Object storage: MinIO, AWS S3, LakeFS
└── 00-service-stack-demo/          # POC applications (never published)
    ├── 01-multitenancy/            # Four tenancy strategy demos
    ├── 02-jpa-patterns/            # JSONB columns, timeline/audit patterns
    ├── 03-object-storage/          # MinIO/S3 and file/image storage demos
    ├── 04-ai-integration/          # Ollama / OpenAI LLM API integration
    └── 05-kafka-integration/       # Kafka producer/consumer patterns
```

### Version Prefixes

| Prefix | Layer | Published |
|--------|-------|-----------|
| `WS-*` | Stack library modules | ✅ GitHub Packages |
| `WC-*` | Demo / POC applications | ❌ Never |

---

## Module Dependency Chain

```
service-stack-shared               ← foundation (no stack deps)
    └── service-stack-jpa          ← Hibernate ORM runtime
            └── service-stack-crypt      ← JWT + crypto layer
                    └── service-stack-web      ← full web/API layer

service-stack-quartz          ← depends on: shared
service-stack-cassandra       ← depends on: shared, jpa
service-stack-multitenancy    ← depends on: shared, jpa
service-stack-storage         ← depends on: shared
```

---

## Getting Started

### Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Git | Latest | https://git-scm.com/downloads |
| IntelliJ IDEA | Latest (Ultimate or Community) | https://www.jetbrains.com/idea/download |
| JDK | 17+ | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org/download.cgi |
| Docker Desktop | Latest | https://www.docker.com/products/docker-desktop |

---

### GitHub Packages Authentication

Stack library modules are published to GitHub Packages. Maven needs your GitHub
credentials to resolve them.

1. Generate a **Personal Access Token** with `read:packages` scope at
   https://github.com/settings/tokens

2. Add the following to `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>isygo-it-github-01</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

> The `<id>` must be exactly `isygo-it-github-01` to match the repository configuration.

---

### Building

```bash
# Clone
git clone https://github.com/isygo-dev/01-wams-stack.git
cd 01-wams-stack

# Build all modules, skip tests
mvn clean install -DskipTests

# Build with tests (Docker required for Testcontainers)
mvn clean install

# Build in parallel
mvn clean install -DskipTests -T 8

# Build a specific module and its dependencies
mvn clean install -DskipTests -pl 05-service-stack-web -am
```

### Running a POC Application

```bash
cd 00-service-stack-demo/01-multitenancy/01-multitenancy-discriminator
mvn spring-boot:run
```

---

## Using Stack Modules in Your Microservice

Declare `service-stack-parent` as your application's parent:

```xml
<parent>
    <groupId>eu.isygo-it.services</groupId>
    <artifactId>service-stack-parent</artifactId>
    <version>WS-1.0.250603-T1554</version>
</parent>
```

Then add only the modules your service needs — no `<version>` tags required:

```xml
<dependencies>
    <!-- Web layer: Spring Security, Kafka, JWT, Feign, Micrometer, OpenAPI -->
    <dependency>
        <groupId>eu.isygo-it.services</groupId>
        <artifactId>service-stack-web</artifactId>
    </dependency>

    <!-- Multi-tenancy support -->
    <dependency>
        <groupId>eu.isygo-it.services</groupId>
        <artifactId>service-stack-multitenancy</artifactId>
    </dependency>

    <!-- Object storage: MinIO / AWS S3 / LakeFS -->
    <dependency>
        <groupId>eu.isygo-it.services</groupId>
        <artifactId>service-stack-storage</artifactId>
    </dependency>

    <!-- Distributed job scheduling -->
    <dependency>
        <groupId>eu.isygo-it.services</groupId>
        <artifactId>service-stack-quartz</artifactId>
    </dependency>

    <!-- Cassandra NoSQL persistence -->
    <dependency>
        <groupId>eu.isygo-it.services</groupId>
        <artifactId>service-stack-cassandra</artifactId>
    </dependency>
</dependencies>
```

---

## Stack Module Reference

### `service-stack-shared`
Universal foundation consumed by every other stack module. Provides base domain model
classes, DTO abstractions, Jackson serialization (JSON, XML, YAML, CSV), Jakarta Bean
Validation, Apache Camel Spring Boot starters, OpenFeign with Apache HC5 transport,
SpringDoc OpenAPI 3, JSON Schema validation, and common utilities (commons-io, joda-time).

### `service-stack-jpa`
Full Hibernate 6 ORM + Spring Data JPA runtime. Provides repository abstractions, query
derivation, Specifications API, Hibernate Validator, Spring Data auditing
(`@CreatedBy`, `@LastModifiedBy`, `@CreatedDate`, `@LastModifiedDate`), and dynamic
JPQL support via Apache Commons Text.

### `service-stack-crypt`
Cryptography and token management. Provides password encoding via Spring Security Crypto
(BCrypt, Argon2, SCrypt, PBKDF2), JWT creation and validation via JJWT 0.12
(`jjwt-api` on compile scope, `jjwt-impl` and `jjwt-jackson` on runtime scope),
symmetric encryption via Jasypt, and date/time utilities via Joda-Time.

### `service-stack-web`
Complete web and API layer. Provides Spring Security 6.x filter chain configuration,
JWT authentication filter, Spring Kafka producer/consumer templates, Apache Camel Kafka
routes, Spring Retry (`@Retryable`), Micrometer Prometheus metrics
(`/actuator/prometheus`), SpringDoc Swagger UI, Spring JMS with Jakarta JMS API,
PostgreSQL JDBC driver, and HikariCP connection pool.

### `service-stack-quartz`
Distributed Quartz Scheduler integration. Provides auto-configured `SchedulerFactoryBean`
with Spring bean job injection, JDBC JobStore support for persistent clustered scheduling,
and Joda-Time utilities for cron expression building. Quartz manages its own schema
and connection pool independently of the application JPA datasource.

### `service-stack-cassandra`
Spring Data Cassandra integration. Provides `CassandraTemplate`,
`ReactiveCassandraTemplate`, `CassandraRepository`, entity mapping annotations
(`@Table`, `@PrimaryKey`, `@PrimaryKeyColumn`), and `CassandraMappingContext`.
Requires DataStax Java Driver 4.17+.

### `service-stack-multitenancy`
Pluggable multi-tenancy infrastructure supporting four isolation strategies:

| Strategy | Isolation | Description |
|---|---|---|
| `DISCRIMINATOR` | Low | Shared schema, `tenant_id` column on every table |
| `SCHEMA` | Medium | Shared database, one PostgreSQL schema per tenant |
| `DATABASE` | Strong | Dedicated database per tenant, HikariCP pool per tenant |
| `GDM` | Hybrid | Generic Discriminator Model — per-entity-type strategy selection at runtime |

Provides `TenantContext` (ThreadLocal), `CurrentTenantIdentifierResolver`,
`MultiTenantConnectionProvider`, `TenantRoutingDataSource`, per-tenant HikariCP pool
lifecycle management, and a Jakarta Servlet filter for tenant resolution from HTTP
headers, JWT claims, or subdomain.

### `service-stack-storage`
Provider-agnostic object storage abstraction with three pluggable backends:

| Backend | Artifact | Use Case |
|---|---|---|
| MinIO | `io.minio:minio:8.5.17` | Self-hosted S3-compatible storage |
| AWS S3 | `software.amazon.awssdk:s3:2.30.31` | Cloud object storage |
| LakeFS | `io.lakefs:sdk:1.52.0` | Git-like versioned data lake |

Backend selected via `isygo.storage.provider` configuration property.

---

## Demo Modules Reference

Demo modules are runnable Spring Boot applications illustrating stack capabilities.
They are never published to GitHub Packages.

| Module | Demonstrates |
|---|---|
| `poc-multitenancy-discriminator` | Hibernate `@TenantId` / `@Filter` discriminator strategy |
| `poc-multitenancy-schema` | PostgreSQL `search_path` switching per tenant |
| `poc-multitenancy-database` | Dynamic HikariCP pool creation per tenant |
| `poc-multitenancy-gdm` | Per-entity-type isolation strategy selection |
| `poc-json-based-data-pattern` | JSONB columns, `@JdbcTypeCode(SqlTypes.JSON)`, GIN indexes |
| `poc-timeline-events` | Audit trail, event sourcing lite, soft-delete with effective dating |
| `poc-object-storage-solutions` | MinIO / S3 / LakeFS unified API comparison |
| `poc-file-image-storage` | Multi-tenant file upload, streaming download, presigned URLs |
| `poc-open-ai-apis` | Ollama/OpenAI REST client, PDF text extraction via PDFBox 3.x |
| `poc-producer-consumer` | Spring Kafka producer/consumer, dead-letter topics, Testcontainers Kafka |

---

## Maven Enforcer Rules

The following rules run on every build before compilation:

| Rule | Requirement |
|---|---|
| Maven version | 3.9 or higher |
| Java version | 17 or higher |
| Release dependencies | No SNAPSHOT deps allowed in release builds |
| Duplicate versions | No dependency declared twice with different versions |

```bash
# Skip enforcer for local development only — never skip in CI
mvn install -Denforcer.skip=true
```

---

## Useful Commands

```bash
# Check available dependency updates
mvn versions:display-dependency-updates

# Check available plugin updates
mvn versions:display-plugin-updates

# View effective POM for a module
mvn help:effective-pom -pl 05-service-stack-web

# View dependency tree for a module
mvn dependency:tree -pl 05-service-stack-web

# Publish stack modules to GitHub Packages
mvn clean deploy -DskipTests
```

---

## License

Proprietary — Isygo-IT Services · https://isygo-it.eu