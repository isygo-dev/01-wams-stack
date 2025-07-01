package eu.isygoit.multitenancy.common;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Data source factory.
 */
@Configuration
public class DataSourceFactory {

    /**
     * Tenant data sources map.
     *
     * @param properties the properties
     * @return the map
     */
    @Bean(name = "tenantDataSources")
    public Map<String, DataSource> tenantDataSources(MultiTenancyProperties properties) {
        Map<String, DataSource> map = new HashMap<>();
        for (TenantDataSourceConfig tenant : properties.getTenants()) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(tenant.getUrl());
            config.setUsername(tenant.getUsername());
            config.setPassword(tenant.getPassword());
            config.setPoolName("ds-" + tenant.getId());
            map.put(tenant.getId().toLowerCase(), new HikariDataSource(config));
        }
        return map;
    }

    /**
     * Default data source data source.
     *
     * @param properties the properties
     * @return the data source
     */
    @Bean
    public DataSource defaultDataSource(MultiTenancyProperties properties) {
        // Use the first tenant as default
        TenantDataSourceConfig tenant = properties.getTenants().get(0);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(tenant.getUrl());
        config.setUsername(tenant.getUsername());
        config.setPassword(tenant.getPassword());
        config.setPoolName("default-ds");

        return new HikariDataSource(config);
    }
}
