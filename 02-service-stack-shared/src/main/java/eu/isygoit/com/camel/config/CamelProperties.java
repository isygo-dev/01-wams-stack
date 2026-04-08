package eu.isygoit.com.camel.config;

import lombok.Getter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Camel properties.
 */
@Getter
@ConditionalOnProperty(name = "app.camel.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "app.camel")
public class CamelProperties {

}
