package eu.isygoit.multitenancy.database;

import eu.isygoit.multitenancy.common.TenantIdentifierResolver;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.multiTenancy", havingValue = "DATABASE")
@Configuration
public class MultiTenantJpaConfig {

    private final Environment environment;
    private final JpaProperties jpaProperties;

    public MultiTenantJpaConfig(Environment environment, JpaProperties jpaProperties) {
        this.environment = environment;
        this.jpaProperties = jpaProperties;
    }

    @Bean
    public Map<String, DataSource> tenantDataSources() {
        Map<String, DataSource> map = new HashMap<>();

        for (String tenantId : List.of("tenant1", "tenant2")) {
            String prefix = "app.tenants." + tenantId;
            String url = environment.getProperty(prefix + ".url");
            String username = environment.getProperty(prefix + ".username");
            String password = environment.getProperty(prefix + ".password");

            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setDriverClassName("org.postgresql.Driver");

            map.put(tenantId, dataSource);
        }

        return map;
    }

    @Bean
    public DataSourceBasedMultiTenantConnectionProviderImpl multiTenantConnectionProvider() {
        return new DataSourceBasedMultiTenantConnectionProviderImpl(tenantDataSources());
    }

    @Bean
    public TenantIdentifierResolver currentTenantIdentifierResolver() {
        return new TenantIdentifierResolver();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
        Map<String, Object> hibernateProps = new HashMap<>(jpaProperties.getProperties());

        hibernateProps.put("hibernate.multiTenancy", "DATABASE");
        hibernateProps.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider());
        hibernateProps.put("hibernate.tenant_identifier_resolver", currentTenantIdentifierResolver());

        return builder
                .dataSource(tenantDataSources().get("tenant1")) // default
                .packages("com.example.multitenancy") // your base package
                .properties(hibernateProps)
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}

