package eu.isygoit.multitenancy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Service
public class H2TenantService {

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    public H2TenantService(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTenant(String tenantId) {
        try {
            // Check if schema exists in H2
            Boolean schemaExists = jdbcTemplate.queryForObject(
                    "SELECT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?)",
                    Boolean.class, tenantId + "_SCHEMA");

            if (schemaExists == null || !schemaExists) {
                // Create schema in H2
                jdbcTemplate.execute("CREATE SCHEMA " + tenantId + "_SCHEMA");

                // H2 needs explicit permission grants
                jdbcTemplate.execute("GRANT ALL ON SCHEMA " + tenantId + "_SCHEMA" + " TO SA");

                log.info("Created H2 schema for tenant: {}", tenantId);
            }
        } catch (Exception e) {
            log.error("Failed to create H2 schema for tenant: " + tenantId, e);
            throw new RuntimeException("Failed to create tenant schema in H2", e);
        }
    }

    public void initializeTenantSchema(String tenantId) {
        String schema = tenantId + "_SCHEMA";

        // Ensure schema exists
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schema);

        // Switch to the tenant schema
        jdbcTemplate.execute("SET SCHEMA " + schema);

        try {
            // Create SEQUENCE first (optional but preferred if ID uses it)
            jdbcTemplate.execute("""
                        CREATE SEQUENCE IF NOT EXISTS TUTORIALS_SEQ
                        START WITH 1
                        INCREMENT BY 1;
                    """);

            // Create the TUTORIALS table using the SEQUENCE
            jdbcTemplate.execute("""
                        CREATE TABLE IF NOT EXISTS TUTORIALS (
                            ID BIGINT DEFAULT NEXT VALUE FOR TUTORIALS_SEQ PRIMARY KEY,
                            "TENANT_ID" VARCHAR(255) NOT NULL,
                            TITLE VARCHAR(255),
                            DESCRIPTION VARCHAR(1000),
                            PUBLISHED BOOLEAN,
                            CREATE_DATE TIMESTAMP,
                            CREATED_BY VARCHAR(255),
                            UPDATE_DATE TIMESTAMP,
                            UPDATED_BY VARCHAR(255)
                        );
                    """);

        } finally {
            // Always return to PUBLIC schema
            jdbcTemplate.execute("SET SCHEMA PUBLIC");
        }
    }

    public void shutdownH2Database() throws SQLException {

        dataSource.getConnection().commit();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SHUTDOWN COMPACT");
            log.info("H2 database has been shut down gracefully.");
        } catch (Exception ex) {
            log.warn("Failed to shut down H2 database", ex);
        }
    }
}
