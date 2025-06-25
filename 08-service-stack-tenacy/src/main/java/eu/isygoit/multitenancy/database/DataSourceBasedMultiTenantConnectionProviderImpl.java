package eu.isygoit.multitenancy.database;

import eu.isygoit.multitenancy.common.TenantContext;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class DataSourceBasedMultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {

    private final Map<String, DataSource> dataSources;

    public DataSourceBasedMultiTenantConnectionProviderImpl(Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return getConnection(TenantContext.getTenantId());
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        DataSource dataSource = dataSources.get(tenantIdentifier);
        if (dataSource == null) {
            throw new SQLException("Unknown tenant: " + tenantIdentifier);
        }
        return dataSource.getConnection();
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

