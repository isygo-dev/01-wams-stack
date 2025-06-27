package eu.isygoit.multitenancy.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes tenant-specific schema and database objects for the SCHEMA-per-tenant strategy.
 */
@Slf4j
@Service
public class TenantService {

    private final MultiTenantConnectionProvider multiTenantConnectionProvider;

    public TenantService(MultiTenantConnectionProvider multiTenantConnectionProvider) {
        this.multiTenantConnectionProvider = multiTenantConnectionProvider;
    }

    /**
     * Creates schema, sequence, and table for a given tenant (schema name).
     *
     * @param tenantId The tenant schema to initialize.
     */
    public void initializeTenantSchema(String tenantId) {
        try (Connection connection = multiTenantConnectionProvider.getAnyConnection();
             Statement stmt = connection.createStatement()) {

            // Create the schema if it doesn't exist
            stmt.execute("CREATE SCHEMA IF NOT EXISTS \"" + tenantId + "\"");

            // Set the connection to use the tenant schema
            stmt.execute("SET SCHEMA '" + tenantId + "'");

            // Create sequence inside the tenant schema
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

            log.info("Schema '{}' initialized with tutorials table and sequence.", tenantId);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize schema for tenant: " + tenantId, e);
        }
    }
}