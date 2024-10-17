package eu.isygoit.encrypt.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Jasypt properties.
 */
@Data
@ConfigurationProperties(prefix = "jasypt")
public class JasyptProperties {

    @Value("${jasypt.password.default}")
    private String defaultPassword;

    @Value("${jasypt.password.pattern}")
    private String passwordPattern;

    @Value("${jasypt.encryptor.password}")
    private String encryptorPassword;

    @Value("${jasypt.encryptor.algorithm}")
    private String algorithm;

    @Value("${jasypt.encryptor.password.algorithm}")
    private String passwordAlgorithm;

    @Value("${jasypt.encryptor.pool.size}")
    private Integer poolSize;

    @Value("${jasypt.encryptor.salt.generator.class}")
    private String saltGeneratorClassName;

    @Value("${jasypt.encryptor.salt}")
    private Integer salt;

    @Value("${jasypt.generator.key.size}")
    private Integer keyGeneratorSize;
}
