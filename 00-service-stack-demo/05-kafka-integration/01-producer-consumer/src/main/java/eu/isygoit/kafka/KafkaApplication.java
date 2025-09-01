package eu.isygoit.kafka;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * The type Storage application.
 */
@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@EnableConfigurationProperties
//http://localhost:8081/swagger-ui/index.html
@OpenAPIDefinition(info =
@Info(title = "Poc Kafka", version = "1.0", description = "Poc Kafka")
)
public class KafkaApplication {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(KafkaApplication.class, args);
    }

    /**
     * Configure RestTemplate bean for HTTP client operations.
     *
     * @return Configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
