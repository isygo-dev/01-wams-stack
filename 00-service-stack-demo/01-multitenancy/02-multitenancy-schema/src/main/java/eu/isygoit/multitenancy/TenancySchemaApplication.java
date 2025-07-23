package eu.isygoit.multitenancy;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@EnableConfigurationProperties
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EntityScan(basePackages = "eu.isygoit.multitenancy.model")
@OpenAPIDefinition(info =
@Info(title = "Poc multitenancy schema-per-tenant", version = "1.0", description = "Poc multitenancy schema-per-tenant")
)
public class TenancySchemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenancySchemaApplication.class, args);
    }
}
