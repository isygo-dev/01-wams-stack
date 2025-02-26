package eu.isygoit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The type Enc config.
 */
@Configuration

@EnableConfigurationProperties(EncProperties.class)
public class EncConfig {

    private final EncProperties encProperties;

    /**
     * Instantiates a new Enc config.
     *
     * @param encProperties the enc properties
     */
    public EncConfig(EncProperties encProperties) {
        this.encProperties = encProperties;
    }
}
