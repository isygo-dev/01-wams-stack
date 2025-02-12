package eu.isygoit.encrypt.config;

import eu.isygoit.encrypt.generator.IKeyGenerator;
import eu.isygoit.encrypt.generator.KeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jasypt.digest.config.SimpleDigesterConfig;
import org.jasypt.encryption.pbe.*;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.util.password.ConfigurablePasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Configuration class for Jasypt encryption setup.
 * This class defines various encryptor beans and configurations using Jasypt library.
 */
@Configuration
@EnableConfigurationProperties(JasyptProperties.class)
@Slf4j  // Lombok annotation automatically creates a logger named 'log'
public class JasyptConfig {

    /**
     * The constant DEFAULT_ENCRYPTOR_PASSWORD.
     */
    public static final String DEFAULT_ENCRYPTOR_PASSWORD = "defaultEncryptorPassword";
    /**
     * The constant DEFAULT_ALGORITHM.
     */
    public static final String DEFAULT_ALGORITHM = "PBEWithMD5AndDES";
    /**
     * The constant DEFAULT_POOL_SIZE.
     */
    public static final int DEFAULT_POOL_SIZE = 1;
    /**
     * The constant DEFAULT_SALT_GENERATOR_CLASS.
     */
    public static final String DEFAULT_SALT_GENERATOR_CLASS = "org.jasypt.salt.RandomSaltGenerator";
    /**
     * The constant DEFAULT_PASSWORD_ALGORITHM.
     */
    public static final String DEFAULT_PASSWORD_ALGORITHM = "SHA-256";
    /**
     * The constant DEFAULT_PROVIDER_NAME.
     */
    public static final String DEFAULT_PROVIDER_NAME = "SunJCE";
    /**
     * The constant DEFAULT_STRING_OUTPUT_TYPE.
     */
    public static final String DEFAULT_STRING_OUTPUT_TYPE = "base64";
    /**
     * The constant DEFAULT_KEY_OBTENTION_ITERATIONS.
     */
    public static final int DEFAULT_KEY_OBTENTION_ITERATIONS = 1000;
    /**
     * The constant DEFAULT_SALT_SIZE_BYTES.
     */
    public static final int DEFAULT_SALT_SIZE_BYTES = 8;
    /**
     * The constant DEFAULT_INVERT_POSITION_OF_PLAIN_SALT.
     */
    public static final boolean DEFAULT_INVERT_POSITION_OF_PLAIN_SALT = false;
    private final JasyptProperties jasyptProperties;

    /**
     * Constructor to initialize the JasyptConfig with the given JasyptProperties.
     *
     * @param jasyptProperties The Jasypt properties to configure encryption settings.
     */
    public JasyptConfig(JasyptProperties jasyptProperties) {
        this.jasyptProperties = jasyptProperties;
        log.info("JasyptConfig initialized with provided properties: {}", jasyptProperties);
    }

    /**
     * Bean for ConfigurablePasswordEncryptor.
     * Configures a password encryptor with the provided settings.
     *
     * @return ConfigurablePasswordEncryptor the configured password encryptor.
     */
    @Bean(name = "configurablePasswordEncryptor")
    public ConfigurablePasswordEncryptor configurablePasswordEncryptor() {
        var encryptor = new ConfigurablePasswordEncryptor();
        encryptor.setConfig(getPasswordEncryptorConfiguration());
        log.info("ConfigurablePasswordEncryptor bean created and configured.");
        return encryptor;
    }

    /**
     * Bean for configuring the general encryptor settings.
     * Uses default values where applicable.
     *
     * @return SimpleStringPBEConfig the encryptor configuration.
     */
    @Bean
    public SimpleStringPBEConfig getEncryptorConfiguration() {
        var config = new SimpleStringPBEConfig();
        // Use property or default for each setting
        config.setPassword(Optional.ofNullable(jasyptProperties.getEncryptorPassword())
                .orElse(DEFAULT_ENCRYPTOR_PASSWORD));
        config.setAlgorithm(Optional.ofNullable(jasyptProperties.getAlgorithm())
                .orElse(DEFAULT_ALGORITHM));
        config.setKeyObtentionIterations(Optional.ofNullable(jasyptProperties.getKeyObtentionIterations())
                .orElse(DEFAULT_KEY_OBTENTION_ITERATIONS));
        config.setPoolSize(Optional.ofNullable(jasyptProperties.getPoolSize())
                .orElse(DEFAULT_POOL_SIZE));
        config.setProviderName(DEFAULT_PROVIDER_NAME);
        config.setSaltGeneratorClassName(Optional.ofNullable(jasyptProperties.getSaltGeneratorClassName())
                .orElse(DEFAULT_SALT_GENERATOR_CLASS));
        config.setStringOutputType(DEFAULT_STRING_OUTPUT_TYPE);

        // Use Optional to safely handle salt generator
        Optional.ofNullable(config.getSaltGenerator())
                .ifPresent(saltGen -> saltGen.generateSalt(jasyptProperties.getSalt()));

        log.info("Encryptor configuration initialized with algorithm: {}, pool size: {}",
                config.getAlgorithm(), config.getPoolSize());
        return config;
    }

    /**
     * Bean for GUID generator.
     * Configures a key generator for GUIDs with size from properties or defaults.
     *
     * @return IKeyGenerator the GUID generator.
     */
    @Bean
    public IKeyGenerator getGuidGenerator() {
        log.info("GUID Generator bean created with size: {}", jasyptProperties.getKeyGeneratorSize());
        return new KeyGenerator(jasyptProperties.getKeyGeneratorSize());
    }

    /**
     * Bean for password encryptor configuration.
     * Uses a default password algorithm when none is specified.
     *
     * @return SimpleDigesterConfig the password encryptor configuration.
     */
    @Bean
    public SimpleDigesterConfig getPasswordEncryptorConfiguration() {
        var config = new SimpleDigesterConfig();
        // Use default or provided password algorithm
        config.setAlgorithm(Optional.ofNullable(jasyptProperties.getPasswordAlgorithm())
                .orElse(DEFAULT_PASSWORD_ALGORITHM));
        config.setSaltGeneratorClassName(Optional.ofNullable(jasyptProperties.getSaltGeneratorClassName())
                .orElse(DEFAULT_SALT_GENERATOR_CLASS));
        config.setSaltSizeBytes(DEFAULT_SALT_SIZE_BYTES);
        config.setInvertPositionOfPlainSaltInEncryptionResults(DEFAULT_INVERT_POSITION_OF_PLAIN_SALT);

        // Using Optional to safely handle salt generator
        Optional.ofNullable(config.getSaltGenerator())
                .ifPresent(saltGen -> saltGen.generateSalt(jasyptProperties.getSalt()));

        log.info("Password encryptor configuration initialized with algorithm: {}",
                config.getAlgorithm());
        return config;
    }

    /**
     * Bean for StrongPasswordEncryptor.
     *
     * @return StrongPasswordEncryptor the strong password encryptor.
     */
    @Bean(name = "passwordEncryptor")
    public StrongPasswordEncryptor passwordEncryptor() {
        log.info("StrongPasswordEncryptor bean created.");
        return new StrongPasswordEncryptor();
    }

    /**
     * Bean for PooledPBEBigDecimalEncryptor.
     * Configures the encryptor using the provided settings or defaults.
     *
     * @return PooledPBEBigDecimalEncryptor the pooled BigDecimal encryptor.
     */
    @Bean(name = "pooledBigDecimalEncryptor")
    public PooledPBEBigDecimalEncryptor pooledPBEBigDecimalEncryptor() {
        var encryptor = new PooledPBEBigDecimalEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        log.info("PooledPBEBigDecimalEncryptor bean created and configured.");
        return encryptor;
    }

    /**
     * Bean for PooledPBEBigIntegerEncryptor.
     * Configures the encryptor using the provided settings or defaults.
     *
     * @return PooledPBEBigIntegerEncryptor the pooled BigInteger encryptor.
     */
    @Bean(name = "pooledBigIntegerEncryptor")
    public PooledPBEBigIntegerEncryptor pooledPBEBigIntegerEncryptor() {
        var encryptor = new PooledPBEBigIntegerEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        log.info("PooledPBEBigIntegerEncryptor bean created and configured.");
        return encryptor;
    }

    /**
     * Bean for PooledPBEByteEncryptor.
     * Configures the encryptor using the provided settings or defaults.
     *
     * @return PooledPBEByteEncryptor the pooled Byte encryptor.
     */
    @Bean(name = "pooledByteEncryptor")
    public PooledPBEByteEncryptor pooledPBEByteEncryptor() {
        var encryptor = new PooledPBEByteEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        log.info("PooledPBEByteEncryptor bean created and configured.");
        return encryptor;
    }

    /**
     * Bean for PooledPBEStringEncryptor.
     * Configures the encryptor using the provided settings or defaults.
     *
     * @return PooledPBEStringEncryptor the pooled String encryptor.
     */
    @Bean(name = "pooledStringEncryptor")
    public PooledPBEStringEncryptor pooledPBEStringEncryptor() {
        var encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        log.info("PooledPBEStringEncryptor bean created and configured.");
        return encryptor;
    }

    /**
     * Bean for StandardPBEBigDecimalEncryptor.
     * Configures the encryptor using the provided settings or defaults.
     *
     * @return StandardPBEBigDecimalEncryptor the standard BigDecimal encryptor.
     */
    @Bean(name = "standardPBEBigDecimalEncryptor")
    public StandardPBEBigDecimalEncryptor standardPBEBigDecimalEncryptor() {
        var encryptor = new StandardPBEBigDecimalEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        log.info("StandardPBEBigDecimalEncryptor bean created and configured.");
        return encryptor;
    }

    /**
     * Bean for StandardPBEBigIntegerEncryptor.
     * Configures the encryptor using the provided settings or defaults.
     *
     * @return StandardPBEBigIntegerEncryptor the standard BigInteger encryptor.
     */
    @Bean(name = "standardPBEBigIntegerEncryptor")
    public StandardPBEBigIntegerEncryptor standardPBEBigIntegerEncryptor() {
        var encryptor = new StandardPBEBigIntegerEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        log.info("StandardPBEBigIntegerEncryptor bean created and configured.");
        return encryptor;
    }

    /**
     * Bean for StandardPBEByteEncryptor.
     * Configures the encryptor using the provided settings or defaults.
     *
     * @return StandardPBEByteEncryptor the standard Byte encryptor.
     */
    @Bean(name = "standardPBEByteEncryptor")
    public StandardPBEByteEncryptor standardPBEByteEncryptor() {
        var encryptor = new StandardPBEByteEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        log.info("StandardPBEByteEncryptor bean created and configured.");
        return encryptor;
    }

    /**
     * Bean for StandardPBEStringEncryptor.
     * Configures the encryptor using the provided settings or defaults.
     *
     * @return StandardPBEStringEncryptor the standard String encryptor.
     */
    @Bean(name = "standardPBEStringEncryptor")
    public StandardPBEStringEncryptor standardPBEStringEncryptor() {
        var encryptor = new StandardPBEStringEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        log.info("StandardPBEStringEncryptor bean created and configured.");
        return encryptor;
    }
}