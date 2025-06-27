package eu.isygoit.multitenancy;

import eu.isygoit.multitenancy.service.TenantService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@EnableConfigurationProperties
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EntityScan(basePackages = "eu.isygoit.multitenancy.model")
@EnableJpaRepositories(basePackages = "eu.isygoit.multitenancy.repository")
@OpenAPIDefinition(info =
@Info(title = "Poc Multi-tenacy schema-per-tenant", version = "1.0", description = "Poc Multi-tenacy schema-per-tenant")
)
public class MultiTenancyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiTenancyApplication.class, args);
    }

    @Bean
    public CommandLineRunner initH2Schemas(TenantService tenantService) {
        return args -> {
            // Create your initial tenants
            tenantService.initializeTenantSchema("tenant1");
            tenantService.initializeTenantSchema("tenant2");
        };
    }
}
