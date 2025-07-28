package eu.isygoit.database;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * The type Database multi tenant connection provider.
 */
@Component
@ConditionalOnExpression(
        "'${app.tenancy.enabled}'=='true' && '${app.tenancy.mode}'=='DATABASE'"
)
public class DatabaseMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final Map<String, DataSource> tenantDataSources;

    /**
     * Instantiates a new Database multi tenant connection provider.
     *
     * @param tenantDataSources the tenant data sources
     */
    public DatabaseMultiTenantConnectionProvider(@Qualifier("tenantDataSources") Map<String, DataSource> tenantDataSources) {
        this.tenantDataSources = tenantDataSources;
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        DataSource ds = tenantDataSources.get(tenantIdentifier);
        if (ds == null) {
            throw new SQLException("No DataSource found for tenant: " + tenantIdentifier);
        }
        return ds.getConnection();
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return tenantDataSources.values().iterator().next().getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
