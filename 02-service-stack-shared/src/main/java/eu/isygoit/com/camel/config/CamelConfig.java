package eu.isygoit.com.camel.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The type Camel config.
 */
@Configuration
@EnableConfigurationProperties(CamelProperties.class)
public class CamelConfig {

    private final CamelProperties camelProperties;

    /**
     * Instantiates a new Camel config.
     *
     * @param camelProperties the camel properties
     */
    public CamelConfig(CamelProperties camelProperties) {
        this.camelProperties = camelProperties;
    }
}
