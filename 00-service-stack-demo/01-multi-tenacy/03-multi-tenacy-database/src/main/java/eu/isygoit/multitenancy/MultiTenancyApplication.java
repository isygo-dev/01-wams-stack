package eu.isygoit.multitenancy;

import eu.isygoit.multitenancy.service.H2TenantService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
@OpenAPIDefinition(info =
@Info(title = "Poc Multi-tenacy schema-per-tenant", version = "1.0", description = "Poc Multi-tenacy schema-per-tenant")
)
public class MultiTenancyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiTenancyApplication.class, args);
    }

    @Bean
    public CommandLineRunner initH2Schemas(H2TenantService tenantService) {
        return args -> {
            // Create your initial tenants
            tenantService.initializeTenantSchema("TENANT1");
            tenantService.initializeTenantSchema("TENANT2");
        };
    }
}
