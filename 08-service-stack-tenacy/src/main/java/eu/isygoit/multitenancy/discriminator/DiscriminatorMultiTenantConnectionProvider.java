package eu.isygoit.multitenancy.discriminator;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Discriminator-based MultiTenantConnectionProvider implementation.
 * Always returns the same connection since all tenants share the same schema.
 */
@Component
@ConditionalOnExpression(
        "'${multi-tenancy.mode}'=='DISCRIMINATOR' || '${multi-tenancy.mode}'=='GDM'"
)
public class DiscriminatorMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    /**
     * Instantiates a new Discriminator multi tenant connection provider.
     *
     * @param dataSource the data source
     */
    public DiscriminatorMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Always returns the same connection. Filtering is done via Hibernate filter (not schema).
     */
    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        return dataSource.getConnection(); // No schema switch for discriminator strategy
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
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
        return unwrapType.isInstance(this);
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return unwrapType.isInstance(this) ? unwrapType.cast(this) : null;
    }
}