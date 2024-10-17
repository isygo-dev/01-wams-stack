package eu.isygoit.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * The type Com app properties.
 */
@Data
public class ComAppProperties {

    @Value("${app.email.async}")
    private boolean sendAsyncEmail;

    @Value("${app.email.broker}")
    private String throughBroker;

    @Value("${app.upload.directory}")
    private String uploadDirectory;

    @Value("${app.feign.shouldNotFilterKey}")
    private String shouldNotFilterKey;

    @Value("${spring.application.version}")
    private String applicationVersion;

    @Value("${spring.application.name}")
    private String applicationName;
}
