package eu.isygoit.quartz.conf;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Quartz properties.
 */
@Getter
@ConfigurationProperties(prefix = "spring.quartz")
public class QuartzProperties {

}
