package eu.isygoit.multitenancy.common;


import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * The type Multi tenant jpa config.
 */
@Configuration
@EnableTransactionManagement
public class MultiTenantJpaConfig {

    @Value("${multi-tenancy.mode}")
    private String mode;

    @Autowired
    private MultiTenantConnectionProvider multiTenantConnectionProvider;

    /**
     * Entity manager factory local container entity manager factory bean.
     *
     * @param defaultDataSource the default data source
     * @return the local container entity manager factory bean
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource defaultDataSource) {

        var tenantResolver = new TenantIdentifierResolver();

        var vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
        props.put("hibernate.tenant_identifier_resolver", tenantResolver);
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "update");

        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(defaultDataSource);
        em.setPackagesToScan("eu.isygoit.multitenancy.model");
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaPropertyMap(props);

        return em;
    }

    /**
     * Transaction manager platform transaction manager.
     *
     * @param em the em
     * @return the platform transaction manager
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory em) {
        return new JpaTransactionManager(em);
    }
}
