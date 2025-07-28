package eu.isygoit.utils;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Initializes tenant-specific model and database objects for the SCHEMA-per-tenant strategy.
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
     * Creates model, sequence, and table for a given tenant (model name).
     *
     * @param tenantId The tenant model to initialize.
     */
    public void initializeTenantSchema(String tenantId) {
        try (Connection connection = multiTenantConnectionProvider.getAnyConnection();
             Statement stmt = connection.createStatement()) {

            // Load SQL file from classpath
            ClassPathResource resource = new ClassPathResource("db/pg_tenant-schema.sql");
            try (InputStream inputStream = resource.getInputStream()) {
                String rawSql = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

                // Replace the model name placeholder
                String sql = rawSql.replace("public", tenantId);

                // Execute each statement separately
                for (String statement : sql.split(";")) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }

            log.info("Initialized model from SQL script for tenant: {}", tenantId);

        } catch (Exception e) {
            throw new RuntimeException("Error initializing model for tenant: " + tenantId, e);
        }
    }
}