package eu.isygoit;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@EnableConfigurationProperties
//http://localhost:8081/swagger-ui/index.html
@OpenAPIDefinition(info =
@Info(title = "Poc Entity with attachments", version = "1.0", description = "Poc Entity with attachments")
)
public class EntityAttachmentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EntityAttachmentsApplication.class, args);
    }
}
