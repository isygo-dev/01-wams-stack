To address your request for enhancements in modern design, readability, maintainability, security, and efficiency, I have prepared a prioritized plan based on severity. 

This plan is structured into four phases, starting with critical security and maintenance issues, followed by modernization and optimization.

✅ ### Phase 1: High Severity — Security & Data Integrity
These tasks address potential logical vulnerabilities and ensure basic data consistency.

1.  **Harden Criteria Filtering**: 
    *   **Current Issue**: `CriteriaHelper` uses manual string parsing, which could lead to unexpected query execution.
    *   **Task**: Implement a **strict whitelist** of allowed fields for filtering. Validate that requested operators match the field's data type (e.g., prevent `LIKE` on numeric fields).
2.  **Standardize Input Validation**: 
    *   **Current Issue**: Validation is fragmented and often manual (e.g., `validateObjectNotNull`).
    *   **Task**: Transition to **Jakarta Bean Validation** (`@Valid`, `@NotNull`, `@Size`). Annotate DTOs to catch invalid data at the controller entry point before it hits service logic.

✅ ### Phase 2: High Severity — Maintainability & Technical Debt
These tasks focus on reducing code duplication and improving developer efficiency.

1.  **Consolidate CRUD Logic**: 
    *   **Current Issue**: Significant code duplication between `CrudService` and `CrudTenantService`.
    *   **Task**: Move shared business logic (e.g., `validateObjectUpdatable`, `handleEntityCancelation`) into `CrudServiceUtils` or a unified `ServiceHelper`. Use a single service entry point that handles multitenancy context-aware filtering.
2.  **Replace Manual Mapping**: 
    *   **Current Issue**: `keepOriginalAttributes` and other mapping logic use reflection and manual field synchronization.
    *   **Task**: Integrate **MapStruct** with `@MappingTarget`. This provides compile-time safety and better performance than reflection-based synchronization.

✅ ### Phase 3: Medium Severity — Modernization & Readability
These tasks align the codebase with modern Java standards.

1.  **Migrate to Modern Date/Time API**: 
    *   **Current Issue**: Widespread use of `java.util.Date`.
    *   **Task**: Replace with `java.time.Instant` or `java.time.LocalDateTime` in all entities (e.g., `AuditableEntity`, `CancelableEntity`) and DTOs.
2.  **Refactor Dependency Injection**: 
    *   **Current Issue**: Custom annotations like `@InjectService` look up beans manually via reflection.
    *   **Task**: Move toward standard **Spring Constructor Injection**. If generics are a blocker, use abstract factory methods implemented by subclasses.
3.  **Adopt Java Records**: 
    *   **Current Issue**: DTOs and internal data containers have boilerplate getters/setters.
    *   **Task**: Convert immutable data objects (e.g., `QueryCriteria`, `ContextRequestDto`) to **Java Records**.

✅ ### Phase 4: Low Severity — Performance & Optimization
Final refinements to improve the system's runtime efficiency.

1.  **Optimize Filtering DSL**: 
    *   **Current Issue**: Custom SQL-like string parsing is hard to maintain.
    *   **Task**: Transition to a **structured JSON-based filter** format (e.g., `{"field": "name", "op": "eq", "value": "test"}`) to build Spring Specifications directly.
2.  **Enhance Caching Strategy**: 
    *   **Current Issue**: `CriteriaHelper` uses a manual `Map` for field info caching.
    *   **Task**: Implement a robust caching solution like **Caffeine** or Spring’s `@Cacheable` for better thread safety and eviction policies.
3.  **Minimize Runtime Reflection**: 
    *   **Task**: Replace runtime calls to `ParameterizedType` and `FieldAccessorCache` with code-generated solutions or static metadata where possible.