package eu.isygoit.multitenancy.schema;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.multiTenancy", havingValue = "SCHEMA")
@Configuration
public class MultiTenantJpaConfig {

    private final DataSource dataSource;
    private final Environment environment;
    private final JpaProperties jpaProperties;

    public MultiTenantJpaConfig(DataSource dataSource, Environment environment, JpaProperties jpaProperties) {
        this.dataSource = dataSource;
        this.environment = environment;
        this.jpaProperties = jpaProperties;
    }

    @Bean
    public Map<String, String> tenantSchemas() {
        // Load schemas from environment or yml
        Map<String, String> map = new HashMap<>();
        map.put("tenant1", environment.getProperty("app.tenants.tenant1.schema", "public"));
        map.put("tenant2", environment.getProperty("app.tenants.tenant2.schema", "public"));
        return map;
    }

    @Bean
    public DataSourceBasedMultiTenantConnectionProviderImpl multiTenantConnectionProvider() {
        return new DataSourceBasedMultiTenantConnectionProviderImpl(dataSource, tenantSchemas());
    }

    @Bean
    public SchemaCurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
        return new SchemaCurrentTenantIdentifierResolver();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {

        Map<String, Object> hibernateProps = new HashMap<>(jpaProperties.getProperties());

        // Hibernate multi-tenancy properties using string keys
        hibernateProps.put("hibernate.multiTenancy", "SCHEMA");
        hibernateProps.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider());
        hibernateProps.put("hibernate.tenant_identifier_resolver", currentTenantIdentifierResolver());

        return builder
                .dataSource(dataSource)
                .packages("com.example.multitenancy") // your base package
                .properties(hibernateProps)
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
