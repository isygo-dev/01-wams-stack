package eu.isygoit.encrypt.config;

import eu.isygoit.encrypt.generator.IKeyGenerator;
import eu.isygoit.encrypt.generator.KeyGenerator;
import org.jasypt.digest.config.SimpleDigesterConfig;
import org.jasypt.encryption.pbe.*;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.salt.SaltGenerator;
import org.jasypt.util.password.ConfigurablePasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Jasypt config.
 */
@Configuration
@EnableConfigurationProperties(JasyptProperties.class)
public class JasyptConfig {

    private final JasyptProperties jasyptProperties;

    /**
     * Instantiates a new Jasypt config.
     *
     * @param jasyptProperties the jasypt properties
     */
    public JasyptConfig(JasyptProperties jasyptProperties) {
        this.jasyptProperties = jasyptProperties;
    }

    /**
     * Configurable password encryptor configurable password encryptor.
     *
     * @return the configurable password encryptor
     */
    @Bean(name = "configurablePasswordEncryptor")
    public ConfigurablePasswordEncryptor configurablePasswordEncryptor() {
        ConfigurablePasswordEncryptor encryptor = new ConfigurablePasswordEncryptor();
        encryptor.setConfig(getPasswordEncryptorConfiguration());
        return encryptor;
    }

    /**
     * Gets encryptor configuration.
     *
     * @return the encryptor configuration
     */
    @Bean
    public SimpleStringPBEConfig getEncryptorConfiguration() {
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(jasyptProperties.getEncryptorPassword());
        config.setAlgorithm(jasyptProperties.getAlgorithm());
        config.setKeyObtentionIterations("1000");
        config.setPoolSize(jasyptProperties.getPoolSize());
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName(jasyptProperties.getSaltGeneratorClassName());
        SaltGenerator saltGenerator = config.getSaltGenerator();
        saltGenerator.generateSalt(jasyptProperties.getSalt());
        config.setStringOutputType("base64");
        return config;
    }

    /**
     * Gets guid generator.
     *
     * @return the guid generator
     */
    @Bean
    public IKeyGenerator getGuidGenerator() {
        return new KeyGenerator(jasyptProperties.getKeyGeneratorSize());
    }

    /**
     * Gets password encryptor configuration.
     *
     * @return the password encryptor configuration
     */
    @Bean
    public SimpleDigesterConfig getPasswordEncryptorConfiguration() {
        SimpleDigesterConfig simpleDigesterConfig = new SimpleDigesterConfig();
        // DIGEST ALGORITHMS: [MD2, MD5, SHA, SHA-256, SHA-384, SHA-512]
        simpleDigesterConfig.setAlgorithm(jasyptProperties.getPasswordAlgorithm());
        simpleDigesterConfig.setSaltGeneratorClassName(jasyptProperties.getSaltGeneratorClassName());
        SaltGenerator saltGenerator = simpleDigesterConfig.getSaltGenerator();
        saltGenerator.generateSalt(jasyptProperties.getSalt());
        simpleDigesterConfig.setSaltSizeBytes(8);
        simpleDigesterConfig.setInvertPositionOfPlainSaltInEncryptionResults(false);
        return simpleDigesterConfig;
    }

    /**
     * Password encryptor strong password encryptor.
     *
     * @return the strong password encryptor
     */
    @Bean(name = "passwordEncryptor")
    public StrongPasswordEncryptor passwordEncryptor() {
        StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
        return encryptor;
    }

    /**
     * Pooled pbe big decimal encryptor pooled pbe big decimal encryptor.
     *
     * @return the pooled pbe big decimal encryptor
     */
    @Bean(name = "pooledBigDecimalEncryptor")
    public PooledPBEBigDecimalEncryptor pooledPBEBigDecimalEncryptor() {
        PooledPBEBigDecimalEncryptor encryptor = new PooledPBEBigDecimalEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        return encryptor;
    }

    /**
     * Pooled pbe big integer encryptor pooled pbe big integer encryptor.
     *
     * @return the pooled pbe big integer encryptor
     */
    @Bean(name = "pooledBigIntegerEncryptor")
    public PooledPBEBigIntegerEncryptor pooledPBEBigIntegerEncryptor() {
        PooledPBEBigIntegerEncryptor encryptor = new PooledPBEBigIntegerEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        return encryptor;
    }

    /**
     * Pooled pbe byte encryptor pooled pbe byte encryptor.
     *
     * @return the pooled pbe byte encryptor
     */
    @Bean(name = "pooledByteEncryptor")
    public PooledPBEByteEncryptor pooledPBEByteEncryptor() {
        PooledPBEByteEncryptor encryptor = new PooledPBEByteEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        return encryptor;
    }

    /**
     * Pooled pbe string encryptor pooled pbe string encryptor.
     *
     * @return the pooled pbe string encryptor
     */
    @Bean(name = "pooledStringEncryptor")
    public PooledPBEStringEncryptor pooledPBEStringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        return encryptor;
    }

    /**
     * Standard pbe big decimal encryptor standard pbe big decimal encryptor.
     *
     * @return the standard pbe big decimal encryptor
     */
    @Bean(name = "standardPBEBigDecimalEncryptor")
    public StandardPBEBigDecimalEncryptor standardPBEBigDecimalEncryptor() {
        StandardPBEBigDecimalEncryptor encryptor = new StandardPBEBigDecimalEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        return encryptor;
    }

    /**
     * Standard pbe big integer encryptor standard pbe big integer encryptor.
     *
     * @return the standard pbe big integer encryptor
     */
    @Bean(name = "standardPBEBigIntegerEncryptor")
    public StandardPBEBigIntegerEncryptor standardPBEBigIntegerEncryptor() {
        StandardPBEBigIntegerEncryptor encryptor = new StandardPBEBigIntegerEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        return encryptor;
    }

    /**
     * Standard pbe byte encryptor standard pbe byte encryptor.
     *
     * @return the standard pbe byte encryptor
     */
    @Bean(name = "standardPBEByteEncryptor")
    public StandardPBEByteEncryptor standardPBEByteEncryptor() {
        StandardPBEByteEncryptor encryptor = new StandardPBEByteEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        return encryptor;
    }

    /**
     * Standard pbe string encryptor standard pbe string encryptor.
     *
     * @return the standard pbe string encryptor
     */
    @Bean(name = "standardPBEStringEncryptor")
    public StandardPBEStringEncryptor standardPBEStringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setConfig(getEncryptorConfiguration());
        return encryptor;
    }
}
