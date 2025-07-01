# Multi-Tenancy Implementation

A comprehensive Spring Boot multi-tenancy solution supporting four different isolation strategies: Database, Schema,
Discriminator-based, and Generic Discriminator Multi-tenancy (GDM).

## Features

- **Multiple Tenancy Strategies**: Supports DATABASE, SCHEMA, DISCRIMINATOR, and GDM modes
- **Flexible Configuration**: Property-based configuration for easy switching between strategies
- **Thread-Safe**: Uses ThreadLocal for tenant context management
- **Rich Context Management**: Comprehensive request context with user and tenant information
- **Database Support**: Works with PostgreSQL and H2 databases
- **Hibernate Integration**: Seamless integration with Hibernate's multi-tenancy features
- **Request Filtering**: Automatic tenant extraction from HTTP headers
- **Entity Listener**: Automatic tenant assignment to entities
- **Audit Support**: Built-in support for audit trails with formatted user identifiers

## Architecture Overview

The implementation consists of several key components:

### Core Components

1. **TenantContext**: Thread-local storage for current tenant information
2. **RequestContextDto**: Data transfer object for request context information
3. **Connection Providers**: Database-specific connection management for each strategy
4. **Filters**: HTTP request processing for tenant extraction and validation
5. **Configuration**: Auto-configuration based on properties

### Tenancy Strategies

#### 1. DATABASE Strategy

- **Isolation Level**: Complete database separation
- **Use Case**: High security requirements, complete data isolation
- **Implementation**: Each tenant has its own database with separate DataSource

#### 2. SCHEMA Strategy

- **Isolation Level**: Schema-level separation within the same database
- **Use Case**: Moderate isolation with shared database infrastructure
- **Implementation**: Single database with multiple schemas, dynamic schema switching

#### 3. DISCRIMINATOR Strategy

- **Isolation Level**: Row-level separation using tenant columns
- **Use Case**: Shared schema with logical separation
- **Implementation**: Hibernate filters with tenant discriminator columns

#### 4. GDM (Generic Discriminator Multi-tenancy) Strategy

- **Isolation Level**: Row-level separation with generic implementation
- **Use Case**: Flexible discriminator-based approach with enhanced features
- **Implementation**: Extended discriminator pattern with generic tenant handling

## Configuration

### Application Properties

```yaml
# Multi-tenancy configuration
multi-tenancy:
  mode: DATABASE  # Options: DATABASE, SCHEMA, DISCRIMINATOR, GDM
  filter: TENANT  # Options: TENANT, CONTEXT
  tenants:
    - id: tenant1
      url: jdbc:postgresql://localhost:5432/tenant1_db
      username: tenant1_user
      password: tenant1_pass
    - id: tenant2
      url: jdbc:postgresql://localhost:5432/tenant2_db
      username: tenant2_user
      password: tenant2_pass
```

### Tenant Configuration Options

| Mode            | Description                         | Database Support | Isolation Level |
|-----------------|-------------------------------------|------------------|-----------------|
| `DATABASE`      | Separate database per tenant        | PostgreSQL, H2   | Complete        |
| `SCHEMA`        | Separate schema per tenant          | PostgreSQL, H2   | Schema-level    |
| `DISCRIMINATOR` | Shared schema with filters          | PostgreSQL, H2   | Row-level       |
| `GDM`           | Generic Discriminator Multi-tenancy | PostgreSQL, H2   | Row-level       |

### Filter Configuration Options

| Filter Type | Description                            | Context Features       |
|-------------|----------------------------------------|------------------------|
| `TENANT`    | Basic tenant extraction and validation | Tenant ID only         |
| `CONTEXT`   | Enhanced context with user information | Full RequestContextDto |

## Usage

### 1. Entity Configuration

For DISCRIMINATOR and GDM modes, entities should implement the tenant interface:

```java
@Entity
@EntityListeners(TenantEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class MyEntity implements ITenantAssignable {
    
    @Column(name = "tenant_id")
    private String tenant;
    
    // Other fields...
    
    @Override
    public String getTenant() {
        return tenant;
    }
    
    @Override
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
```

### 2. HTTP Requests

Include the tenant identifier in the request header:

```http
GET /api/data
X-Tenant-ID: tenant1
```

### 3. Request Context Access

The system provides rich context information through `RequestContextDto`:

```java
// Access request context (usually set by TenantToContextFilter)
RequestContextDto context = (RequestContextDto) request.getAttribute(JwtConstants.JWT_USER_CONTEXT);

// Context information available:
String tenant = context.getSenderTenant();
String user = context.getSenderUser();
Boolean isAdmin = context.getIsAdmin();
String application = context.getLogApp();

// Get formatted user identifier for auditing
String createdBy = context.getCreatedByString(); // Returns "user@tenant" or "anonymousUser"
```

### 4. Context DTO Structure

```java
RequestContextDto context = RequestContextDto.builder()
    .senderTenant("tenant1")
    .senderUser("john.doe")
    .isAdmin(false)
    .logApp("myapp")
    .build();

// Provides formatted identifier for audit trails
String auditIdentifier = context.getCreatedByString(); // "john.doe@tenant1"
```

### 5. Programmatic Access

```java
// Get current tenant
String currentTenant = TenantContext.getTenantId();

// Set tenant (usually done by filters)
TenantContext.setTenantId("tenant1");

// Clear tenant context
TenantContext.clear();
```

## Implementation Details

### Connection Providers

#### DatabaseMultiTenantConnectionProvider

- Manages separate DataSource instances per tenant
- Routes connections based on tenant identifier
- Suitable for complete database isolation

#### SchemaMultiTenantConnectionProvider

- Uses single DataSource with dynamic schema switching
- Executes `SET SCHEMA` or `SET search_path` commands
- Supports PostgreSQL and H2 databases

#### DiscriminatorMultiTenantConnectionProvider

- Always returns the same connection
- Relies on Hibernate filters for data isolation
- Most resource-efficient approach
- Used for both DISCRIMINATOR and GDM strategies

### Filters

#### TenantFilter

- Extracts tenant ID from `X-Tenant-ID` header
- Validates tenant existence
- Sets tenant context for request duration
- Basic tenant-only filtering

#### TenantToContextFilter

- Extended version that also builds request context
- Creates `RequestContextDto` with tenant and user information
- Adds comprehensive context attributes to request
- Useful for auditing and user tracking across tenants

#### TenantFilterActivationFilter

- Activates Hibernate tenant filters for DISCRIMINATOR and GDM modes
- Ensures proper data filtering at database level

### Request Context DTO

The `RequestContextDto` provides comprehensive context information:

```java
public class RequestContextDto extends AbstractDto {
    private String senderTenant;    // Tenant identifier
    private String senderUser;      // User identifier
    private Boolean isAdmin;        // Admin status
    private String logApp;          // Application identifier
    
    // Returns formatted string: "user@tenant" or "anonymousUser"
    public String getCreatedByString();
}
```

**Key Features:**

- **Audit Trail Support**: `getCreatedByString()` provides formatted identifiers
- **Anonymous User Handling**: Returns "anonymousUser" when user/tenant is empty
- **Extensible**: Built on `AbstractDto` for additional functionality

## Database Support

### PostgreSQL

- Full support for all tenancy strategies
- Uses `search_path` for schema switching
- Recommended for production environments

### H2

- Primarily for development and testing
- Uses `SET SCHEMA` for schema switching
- In-memory and file-based modes supported

## Security Considerations

1. **Tenant Validation**: Always validate tenant identifiers against allowed values
2. **SQL Injection**: Tenant identifiers are used in SQL commands - ensure proper validation
3. **Data Isolation**: Verify that the chosen strategy meets your security requirements
4. **Connection Pooling**: Consider pool sizing for DATABASE strategy
5. **Context Security**: Ensure RequestContextDto doesn't expose sensitive information

## Best Practices

1. **Tenant Validation**: Implement robust tenant validation logic
2. **Error Handling**: Provide clear error messages for invalid tenants
3. **Connection Management**: Properly close connections to prevent leaks
4. **Performance**: Choose the appropriate strategy based on your performance requirements
5. **Monitoring**: Implement monitoring for tenant-specific operations
6. **Audit Logging**: Leverage `RequestContextDto.getCreatedByString()` for consistent audit trails
7. **Context Management**: Use appropriate filter type based on your needs (TENANT vs CONTEXT)

## Troubleshooting

### Common Issues

1. **Missing Tenant Header**: Ensure `X-Tenant-ID` header is included in requests
2. **Invalid Tenant**: Verify tenant exists in configuration
3. **Schema Not Found**: Ensure schemas exist for SCHEMA strategy
4. **Connection Pool Exhaustion**: Monitor connection pool usage in DATABASE strategy
5. **Context Not Available**: Ensure using CONTEXT filter when accessing RequestContextDto

### Debug Configuration

```yaml
logging:
  level:
    eu.isygoit.multitenancy: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## Migration Guide

### From Single-Tenant to Multi-Tenant

1. Add tenant fields to existing entities
2. Update database schema with tenant columns/schemas
3. Configure multi-tenancy properties
4. Update client applications to include tenant headers
5. Test thoroughly with multiple tenants

### Switching Between Strategies

1. Export data from current setup
2. Update configuration properties
3. Restructure database according to new strategy
4. Import data into new structure
5. Update entity annotations if needed

### Upgrading Filter Types

When switching from TENANT to CONTEXT filter:

1. Update `multi-tenancy.filter` property
2. Modify code to use `RequestContextDto` instead of just tenant ID
3. Update any custom filtering logic

## Performance Considerations

| Strategy      | Memory Usage          | Connection Overhead | Query Performance           | Context Overhead |
|---------------|-----------------------|---------------------|-----------------------------|------------------|
| DATABASE      | High (multiple pools) | High                | Excellent                   | Low              |
| SCHEMA        | Medium                | Medium              | Good                        | Low              |
| DISCRIMINATOR | Low                   | Low                 | Good (with proper indexing) | Low              |
| GDM           | Low                   | Low                 | Good (with proper indexing) | Low              |

### Filter Performance

| Filter Type | Overhead | Features                     | Use Case                |
|-------------|----------|------------------------------|-------------------------|
| TENANT      | Minimal  | Basic tenant extraction      | Simple multi-tenancy    |
| CONTEXT     | Low      | Full context + audit support | Enterprise applications |

## Advanced Configuration

### Custom Tenant Validation

```java
@Component
public class CustomTenantValidator implements ITenantValidator {
    
    @Override
    public boolean isValid(String tenantId) {
        // Custom validation logic
        return tenantRepository.existsByCode(tenantId);
    }
}
```

### Custom Context Building

The `TenantToContextFilter` can be extended to include additional context information:

```java
private RequestContextDto buildRequestContext(String tenant, String userName, 
                                            Boolean isAdmin, String application) {
    return RequestContextDto.builder()
            .senderTenant(tenant)
            .senderUser(userName)
            .isAdmin(isAdmin)
            .logApp(application)
            .build();
}
```

## Contributing

When contributing to this multi-tenancy implementation:

1. Ensure all strategies are tested
2. Add appropriate conditional annotations
3. Update documentation for new features
4. Follow existing code patterns
5. Add integration tests for new database support
6. Test context handling for both filter types
7. Verify audit trail functionality

## License

This implementation is part of the ISyGoit framework and follows the project's licensing terms.