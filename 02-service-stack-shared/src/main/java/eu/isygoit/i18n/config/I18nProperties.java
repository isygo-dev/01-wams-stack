package eu.isygoit.i18n.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type 18 n properties.
 */
@Data
@ConditionalOnProperty(name = "app.i18n.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "app.i18n")
public class I18nProperties {

    @Value("${app.i18n.location}")
    private String messagesBaseName;
}
