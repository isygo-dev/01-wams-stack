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

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
@OpenAPIDefinition(info =
@Info(title = "Poc Multi-tenacy schema-per-tenant", version = "1.0", description = "Poc Multi-tenacy schema-per-tenant")
)
public class MultiTenancyApplication {

    @Autowired
    private H2TenantService h2TenantService;

    public static void main(String[] args) {
        SpringApplication.run(MultiTenancyApplication.class, args);
    }

    @Bean
    public CommandLineRunner initH2Schemas(H2TenantService tenantService) {
        return args -> {
            // Create your initial tenants
            tenantService.createTenant("TENANT1");
            tenantService.initializeTenantSchema("TENANT1");

            tenantService.createTenant("TENANT2");
            tenantService.initializeTenantSchema("TENANT2");
        };
    }

    /**
     * Gracefully shuts down the H2 database (flush to disk).
     */
    @PreDestroy
    public void shutdownH2Database() throws SQLException {
        h2TenantService.shutdownH2Database();
    }
}
