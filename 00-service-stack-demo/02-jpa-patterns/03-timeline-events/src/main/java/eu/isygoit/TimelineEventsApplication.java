package eu.isygoit;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * The type Timeline events application.
 */
@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@EnableConfigurationProperties
//http://localhost:8081/swagger-ui/index.html
@OpenAPIDefinition(info =
@Info(title = "Poc timeline events tracking", version = "1.0", description = "Poc timeline events tracking")
)
public class TimelineEventsApplication {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(TimelineEventsApplication.class, args);
    }
}
