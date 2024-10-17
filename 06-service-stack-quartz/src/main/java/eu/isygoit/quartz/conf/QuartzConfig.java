package eu.isygoit.quartz.conf;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The type Quartz config.
 */
@Configuration
@EnableConfigurationProperties(QuartzProperties.class)
public class QuartzConfig {
    private final QuartzProperties quartzProperties;

    /**
     * Instantiates a new Quartz config.
     *
     * @param quartzProperties the quartz properties
     */
    public QuartzConfig(QuartzProperties quartzProperties) {
        this.quartzProperties = quartzProperties;
    }
}
