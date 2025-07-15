package eu.isygoit.openai;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * The type Storage application.
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {
        "eu.isygoit.openai",        // additional package where api resides
        "eu.isygoit.app"
},
        exclude = {
                org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration.class,
                org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        })
@EnableConfigurationProperties
@OpenAPIDefinition(info =
@Info(title = "Poc Open AI", version = "1.0", description = "Poc Open AI")
)
public class OpenAiApplication {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OpenAiApplication.class, args);
    }

    /**
     * Configure RestTemplate bean for HTTP client operations.
     *
     * @return Configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        log.debug("Configuring RestTemplate bean");
        return new RestTemplate();
    }
}
