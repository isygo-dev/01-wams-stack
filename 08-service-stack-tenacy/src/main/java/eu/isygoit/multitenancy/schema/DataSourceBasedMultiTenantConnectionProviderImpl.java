package eu.isygoit.multitenancy.schema;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class DataSourceBasedMultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;
    private final Map<String, String> tenantSchemas;

    public DataSourceBasedMultiTenantConnectionProviderImpl(DataSource dataSource, Map<String, String> tenantSchemas) {
        this.dataSource = dataSource;
        this.tenantSchemas = tenantSchemas;
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
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        String schema = tenantSchemas.getOrDefault(tenantIdentifier, "public");

        final Connection connection = dataSource.getConnection();
        connection.setSchema(schema);
        return connection;
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
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
