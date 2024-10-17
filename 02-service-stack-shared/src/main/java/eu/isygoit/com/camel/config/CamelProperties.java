package eu.isygoit.com.camel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Camel properties.
 */
@Data
@ConfigurationProperties(prefix = "spring.camel")
public class CamelProperties {

}
