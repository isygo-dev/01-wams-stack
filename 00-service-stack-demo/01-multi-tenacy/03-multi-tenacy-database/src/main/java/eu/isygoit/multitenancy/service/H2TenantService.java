package eu.isygoit.multitenancy.service;

import eu.isygoit.multitenancy.database.DatabasePerTenantConnectionProvider;
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

    @Autowired
    private DatabasePerTenantConnectionProvider tenantDataSourceProvider;

    public void initializeTenantSchema(String tenantId) {
        DataSource ds = tenantDataSourceProvider.getDataSource(tenantId);

        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {

            // Create SCHEMA
            stmt.execute("CREATE SCHEMA IF NOT EXISTS PUBLIC");
            stmt.execute("SET SCHEMA PUBLIC");
            stmt.execute("GRANT ALL ON SCHEMA PUBLIC TO SA");

            // Create SEQUENCE first (optional but preferred if ID uses it)
            stmt.execute("""
                        CREATE SEQUENCE IF NOT EXISTS TUTORIALS_SEQ
                        START WITH 1
                        INCREMENT BY 1;
                    """);

            // Create the TUTORIALS table using the SEQUENCE
            stmt.execute("""
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

        } catch (SQLException e) {
            throw new RuntimeException("Error creating DB for tenant " + tenantId, e);
        }
    }
}
