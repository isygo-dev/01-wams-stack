package eu.isygoit.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The type Enc config.
 */
@Configuration
@EnableConfigurationProperties(PwdProperties.class)
@ConditionalOnProperty(name = "app.crypt.enabled", havingValue = "true")
public class CryptConfig {

    private final PwdProperties pwdProperties;

    /**
     * Instantiates a new Enc config.
     *
     * @param pwdProperties the enc properties
     */
    public CryptConfig(PwdProperties pwdProperties) {
        this.pwdProperties = pwdProperties;
    }
}
