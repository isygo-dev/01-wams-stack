package eu.isygoit.multitenancy.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.multiTenancy", havingValue = "DATABASE")
@Configuration
public class MultiTenantJpaConfig {

    @Autowired
    private DatabasePerTenantConnectionProvider tenantDataSourceProvider;

    @Bean
    public DataSource dataSource() {
        MultiTenantRoutingDataSource routingDataSource = new MultiTenantRoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(tenantDataSourceProvider.getDataSource("default"));
        routingDataSource.setLenientFallback(false);
        return routingDataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            EntityManagerFactoryBuilder builder) {

        return builder
                .dataSource(dataSource)
                .packages("eu.isygoit.multitenancy.model") // <-- adapte au bon package
                .persistenceUnit("multiTenantPU")
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
