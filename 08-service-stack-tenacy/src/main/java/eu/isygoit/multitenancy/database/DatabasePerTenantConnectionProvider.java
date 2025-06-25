package eu.isygoit.multitenancy.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.isygoit.multitenancy.common.MultiTenantProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.multiTenancy", havingValue = "DATABASE")
@Component
public class DatabasePerTenantConnectionProvider {

    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();

    @Value("${multi-tenant.db-prefix}")
    private String dbPrefix;

    private final MultiTenantProperties multiTenantProperties;
    private final Environment env;

    public DatabasePerTenantConnectionProvider(MultiTenantProperties multiTenantProperties, Environment env) {
        this.multiTenantProperties = multiTenantProperties;
        this.env = env;
    }

    public DataSource getDataSource(String tenantId) {
        return tenantDataSources.computeIfAbsent(tenantId, this::createDataSource);
    }

    private DataSource createDataSource(String tenantId) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:file:" + dbPrefix + tenantId + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        config.setUsername(env.getProperty("app.tenants." + tenantId + ".username", "sa"));
        config.setPassword(env.getProperty("app.tenants." + tenantId + ".password", ""));
        config.setSchema(env.getProperty("app.tenants." + tenantId + ".schema", "PUBLIC"));
        config.setDriverClassName(env.getProperty("app.tenants." + tenantId + ".driver-class-name", "org.h2.Driver"));

        return new HikariDataSource(config);
    }

    public Set<String> getTenants() {
        return tenantDataSources.keySet();
    }
}
