package eu.isygoit.multitenancy.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Profile("h2")
@Slf4j
@Service
public class H2TenantService implements ITenantService {

    private final MultiTenantConnectionProvider multiTenantConnectionProvider;

    public H2TenantService(MultiTenantConnectionProvider multiTenantConnectionProvider) {
        this.multiTenantConnectionProvider = multiTenantConnectionProvider;
    }

    @Override
    public void initializeTenantSchema(String tenantId) {
        try (Connection connection = multiTenantConnectionProvider.getAnyConnection();
             Statement stmt = connection.createStatement()) {

            stmt.execute("CREATE SCHEMA IF NOT EXISTS \"" + tenantId.toUpperCase() + "\"");

            stmt.execute("SET SCHEMA \"" + tenantId.toUpperCase() + "\"");

            stmt.execute("""
                CREATE SEQUENCE IF NOT EXISTS %s.tutorials_seq
                START WITH 1
                INCREMENT BY 1;
                """.formatted(tenantId));

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS %s.tutorials (
                    id BIGINT DEFAULT NEXT VALUE FOR %s.tutorials_seq,
                    tenant_id VARCHAR(255) NOT NULL,
                    title VARCHAR(255),
                    description VARCHAR(1000),
                    published BOOLEAN,
                    create_date TIMESTAMP,
                    created_by VARCHAR(255),
                    update_date TIMESTAMP,
                    updated_by VARCHAR(255),
                    PRIMARY KEY (id)
                );
                """.formatted(tenantId, tenantId));

            log.info("Initialized H2 schema '{}' with tutorials table.", tenantId);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize H2 schema for tenant: " + tenantId, e);
        }
    }
}
