# 🏗️ Spring Boot Multi-Tenancy Examples

This repository demonstrates multiple strategies for implementing **multi-tenancy** in a Spring Boot application using Hibernate 6 and JPA.

## 🚀 Implemented Multi-Tenancy Strategies

### 1. 🧩 Discriminator-Based Multi-Tenancy 
- Uses a shared schema with a **tenant identifier column** (e.g. `tenant_id`) on every multi-tenant entity.
- Hibernate filters or interceptors are used to isolate tenant data based on the current context.
- **⚠️ Note:** Hibernate 6 deprecated support for discriminator-based multi-tenancy using filters.

### 2. 🏛️ Schema per Tenant
- All tenants share the same database but are **isolated in separate schemas**.
- Implemented using:
    - `MultiTenantConnectionProvider` to resolve connections dynamically.
    - `CurrentTenantIdentifierResolver` to identify the current tenant.
- Tenant-to-schema mapping is loaded from a **YAML configuration** file.
- Enables **runtime schema switching** based on the request context.

### 3. 🗄️ Database per Tenant
- Each tenant has its **own dedicated database**.
- Implemented via dynamic `DataSource` routing:
    - A master registry loads tenant database configurations from a YAML file or a central database.
    - A `MultiTenantDataSource` selects the correct `DataSource` for each tenant.
- Allows full physical isolation of data, offering better security and scalability.

---

## 🧠 Key Concepts Used

- `TenantContext` and `TenantResolver` to resolve the active tenant.
- `AbstractRoutingDataSource` or custom implementations to route `DataSource` per tenant.
- YAML-based configuration (`application.yml`) for declaring tenants, schemas, and database properties.
- Support for both **JPA Repository** and **native queries**.
- Integration with **Spring Boot Profiles** to isolate each multi-tenancy mode.

---

## 🛠️ Technologies

- Java 17
- Spring Boot
- Hibernate 6
- Spring Data JPA
- HikariCP
- H2 / PostgreSQL / MySQL (tested)
- Lombok
- Maven