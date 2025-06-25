package eu.isygoit.multitenancy.schema;

import eu.isygoit.multitenancy.common.MultiTenantProperties;
import eu.isygoit.multitenancy.common.TenantIdentifierResolver;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.multiTenancy", havingValue = "SCHEMA")
@Configuration
@EnableTransactionManagement
public class MultiTenantJpaConfig {

    private final DataSource dataSource;
    private final MultiTenantProperties multiTenantProperties;

    public MultiTenantJpaConfig(DataSource dataSource, MultiTenantProperties multiTenantProperties) {
        this.dataSource = dataSource;
        this.multiTenantProperties = multiTenantProperties;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("eu.isygoit.multitenancy.model");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider());
        properties.put("hibernate.tenant_identifier_resolver", currentTenantIdentifierResolver());

        // Use properties from YAML via MultiTenantProperties bean
        properties.put("hibernate.multiTenancy", multiTenantProperties.getMultiTenancy());
        properties.put("hibernate.dialect", multiTenantProperties.getDialect());
        properties.put("hibernate.show_sql", multiTenantProperties.isShowSql());
        properties.put("hibernate.format_sql", multiTenantProperties.isFormatSql());
        properties.put("hibernate.hbm2ddl.auto", multiTenantProperties.getDdlAuto());

        // Also set username/password from Spring datasource config, no need to hardcode
        // Those can be automatically picked by Spring Boot but you can still specify if needed:
        // e.g. properties.put("hibernate.connection.username", ...);

        properties.put("spring.jpa.hibernate.ddl-auto", multiTenantProperties.getDdlAuto());

        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider() {
        return new SchemaPerTenantConnectionProvider(dataSource);
    }

    @Bean
    public CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
        return new TenantIdentifierResolver();
    }
}
