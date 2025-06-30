package eu.isygoit.multitenancy.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

@Profile("h2")
@Slf4j
@Service
public class H2TenantService implements ITenantService {

    @Autowired
    private MultiTenantConnectionProvider multiTenantConnectionProvider;

    @Override
    public void initializeTenantSchema(String tenantId) {
        try (Connection connection = multiTenantConnectionProvider.getConnection(tenantId);
             Statement stmt = connection.createStatement()) {

            // Load SQL file from classpath
            ClassPathResource resource = new ClassPathResource("db/h2_tenant-schema.sql");
            try (InputStream inputStream = resource.getInputStream()) {
                String sql = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

                // Split and execute each statement
                for (String statement : sql.split(";")) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }

            log.info("Initialized schema from SQL script for tenant: {}", tenantId);

        } catch (Exception e) {
            throw new RuntimeException("Error initializing schema for tenant: " + tenantId, e);
        }
    }
}