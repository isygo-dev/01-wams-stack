package eu.isygoit.multitenancy;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {
        "eu.isygoit.multitenancy",        // additional package where api resides
        "eu.isygoit.multitenancy.api",
        "eu.isygoit.app"
},
        exclude = {
                org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration.class,
                org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@EnableConfigurationProperties
//http://localhost:8081/swagger-ui
@OpenAPIDefinition(info =
@Info(title = "Poc Entity with attachments", version = "1.0", description = "Poc Entity with attachments")
)
public class EntityAttachmentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EntityAttachmentsApplication.class, args);
    }
}
