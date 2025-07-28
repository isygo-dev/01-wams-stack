package eu.isygoit.com.camel.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Camel properties.
 */
@Data
@ConditionalOnProperty(name = "app.camel.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "app.camel")
public class CamelProperties {

}
