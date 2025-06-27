package eu.isygoit.multitenancy.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Profile("h2")
@Slf4j
@Service
public class H2TenantService implements ITenantService {

    @Autowired
    private MultiTenantConnectionProvider tenantDataSourceProvider;

    @Override
    public void initializeTenantSchema(String tenantId) {
        try (Connection connection = tenantDataSourceProvider.getConnection(tenantId);
             Statement stmt = connection.createStatement()) {

            // Create sequence (H2 syntax)
            stmt.execute("""
                    CREATE SEQUENCE IF NOT EXISTS tutorials_seq 
                    START WITH 1 INCREMENT BY 1;
                """);

            // Create table with H2 sequence syntax
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tutorials (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
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

            log.info("Initialized H2 schema and tutorials table for tenant: {}", tenantId);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize schema for tenant: " + tenantId, e);
        }
    }
}