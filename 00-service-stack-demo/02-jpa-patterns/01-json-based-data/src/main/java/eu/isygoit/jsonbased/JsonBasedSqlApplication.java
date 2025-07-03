package eu.isygoit.jsonbased;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "eu.isygoit.multitenancy",     // additional package where service resides
        "eu.isygoit.jsonbased",
        "eu.isygoit.jsonbased.model", // Explicitly include model package
        "eu.isygoit.jsonbased.repository", // Explicitly include repository package
        "eu.isygoit.app"
},
        exclude = {
                org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration.class,
                org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@EntityScan(basePackages = "eu.isygoit.jsonbased.model")
@EnableJpaRepositories(basePackages = "eu.isygoit.jsonbased.repository")
@EnableConfigurationProperties
@OpenAPIDefinition(info =
@Info(title = "Poc Multi-tenacy discriminator-tenant", version = "1.0", description = "Poc Multi-tenacy discriminator-tenant")
)
public class JsonBasedSqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonBasedSqlApplication.class, args);
    }

}
