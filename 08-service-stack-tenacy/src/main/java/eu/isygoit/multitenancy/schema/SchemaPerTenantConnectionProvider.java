package eu.isygoit.multitenancy.schema;

import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class SchemaPerTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    public SchemaPerTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        try {
            // For PostgreSQL
            // connection.createStatement().execute("SET search_path TO " + tenantIdentifier);
            // For H2
            connection.createStatement().execute("SET SCHEMA " + tenantIdentifier + "_SCHEMA");

            // Alternative for other databases:
            // connection.setSchema(tenantIdentifier);
        } catch (SQLException e) {
            connection.close();
            throw new HibernateException("Could not alter JDBC connection to schema [" + tenantIdentifier  + "_SCHEMA" + "]", e);
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            // Postgres Reset to public schema
            //connection.createStatement().execute("SET search_path TO public");
            // H2 Reset to public schema
            connection.createStatement().execute("SET SCHEMA PUBLIC");
        } catch (SQLException e) {
            throw new HibernateException("Could not reset JDBC connection to public schema", e);
        } finally {
            connection.close();
        }
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
    public boolean supportsAggressiveRelease() {
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException();
    }
}

