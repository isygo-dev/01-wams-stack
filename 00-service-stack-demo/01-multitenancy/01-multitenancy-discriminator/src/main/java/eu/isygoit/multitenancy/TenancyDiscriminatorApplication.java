package eu.isygoit.multitenancy;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@OpenAPIDefinition(info =
@Info(title = "Poc multitenancy discriminator-tenant", version = "1.0", description = "Poc multitenancy discriminator-tenant")
)
public class TenancyDiscriminatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenancyDiscriminatorApplication.class, args);
    }

}
