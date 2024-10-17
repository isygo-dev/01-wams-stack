package eu.isygoit.encrypt.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Enc properties.
 */
@Data
@ConfigurationProperties(prefix = "spring.enc")
public class EncProperties {

    @Value("${app.password.pattern}")
    private String passwordPattern;

    @Value("${app.password.grammar.ftl}")
    private String pwdGrammarFtl;
}
