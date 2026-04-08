package eu.isygoit.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Enc properties.
 */
@Getter
@ConfigurationProperties(prefix = "app.crypt.password")
@ConditionalOnProperty(name = "app.crypt.enabled", havingValue = "true")
public class PwdProperties {

    @Value("${app.crypt.password.pattern}")
    private String passwordPattern;

    @Value("${app.crypt.password.grammar.ftl}")
    private String pwdGrammarFtl;
}
