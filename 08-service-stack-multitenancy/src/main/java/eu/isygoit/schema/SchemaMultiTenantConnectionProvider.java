package eu.isygoit.schema;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Schema-based MultiTenantConnectionProvider implementation.
 * Uses a single DataSource and sets the model dynamically per tenant.
 */
@Component
@ConditionalOnExpression(
        "'${app.tenancy.enabled}'=='true' && '${app.tenancy.mode}'=='SCHEMA'"
)
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    /**
     * Instantiates a new Schema multi tenant connection provider.
     *
     * @param dataSource the data source
     */
    public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Called by Hibernate to get a connection for a specific tenant.
     * Sets the model using SET SCHEMA on the JDBC connection.
     */
    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            String dbProductName = connection.getMetaData().getDatabaseProductName().toLowerCase();

            String setSchemaSql;
            if (dbProductName.contains("postgresql")) {
                // PostgreSQL uses search_path
                setSchemaSql = "SET search_path TO " + tenantIdentifier;
            } else if (dbProductName.contains("h2")) {
                // H2 uses SET SCHEMA
                setSchemaSql = "SET SCHEMA " + tenantIdentifier;
            } else {
                throw new SQLException("Unsupported database: " + dbProductName);
            }

            connection.createStatement().execute(setSchemaSql);
        } catch (SQLException e) {
            connection.close();
            throw new SQLException("Could not set model to " + tenantIdentifier, e);
        }

        return connection;
    }

    /**
     * Called by Hibernate for tasks that are not tenant-specific (e.g. model validation).
     */
    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Close connection when not needed anymore (non-tenant-specific).
     */
    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    /**
     * Close connection when tenant-specific tasks are done.
     */
    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        connection.close();
    }

    // Hibernate boilerplate

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return unwrapType.isInstance(this);
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        if (unwrapType.isInstance(this)) {
            return unwrapType.cast(this);
        }
        return null;
    }
}