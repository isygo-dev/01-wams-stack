package eu.isygoit.multitenancy.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes tenant-specific schema and database objects for the SCHEMA-per-tenant strategy.
 */
@Profile("postgres")
@Slf4j
@Service
public class PGTenantService implements ITenantService {
    
    private final MultiTenantConnectionProvider multiTenantConnectionProvider;

    public PGTenantService(MultiTenantConnectionProvider multiTenantConnectionProvider) {
        this.multiTenantConnectionProvider = multiTenantConnectionProvider;
    }

    /**
     * Creates schema, sequence, and table for a given tenant (schema name).
     *
     * @param tenantId The tenant schema to initialize.
     */
    public void initializeTenantSchema(String tenantId) {
        try (Connection connection = multiTenantConnectionProvider.getConnection(tenantId);
             Statement stmt = connection.createStatement()) {

            // Create SCHEMA (optional if schema-per-tenant is not used)
            // For DATABASE mode this may be skipped
            stmt.execute("CREATE SCHEMA IF NOT EXISTS public");

            // PostgreSQL does not use 'SET SCHEMA public' like H2
            // No need to set current schema if you're not using schema-per-tenant

            // Create SEQUENCE
            stmt.execute("""
                        CREATE SEQUENCE IF NOT EXISTS tutorials_seq
                        START WITH 1
                        INCREMENT BY 1
                        NO MINVALUE
                        NO MAXVALUE
                        CACHE 1;
                    """);

            // Create tutorials table inside the tenant schema
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS tutorials (
                            id BIGINT PRIMARY KEY DEFAULT nextval('tutorials_seq'),
                            tenant_id VARCHAR(255) NOT NULL,
                            title VARCHAR(255),
                            description VARCHAR(1000),
                            published BOOLEAN,
                            create_date TIMESTAMP,
                            created_by VARCHAR(255),
                            update_date TIMESTAMP,
                            updated_by VARCHAR(255)
                        );
                    """);

            log.info("Initialized schema and tutorials table for tenant: {}", tenantId);

        } catch (SQLException e) {
            throw new RuntimeException("Error creating schema/table for tenant: " + tenantId, e);
        }
    }
}
