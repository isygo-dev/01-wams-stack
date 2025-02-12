package eu.isygoit.i18n.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * The type 18 n config.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(I18nProperties.class)
public class I18nConfig {

    private final I18nProperties i18nProperties;

    /**
     * Instantiates a new 18 n config.
     *
     * @param i18nProperties the 18 n properties
     */
    public I18nConfig(I18nProperties i18nProperties) {
        this.i18nProperties = i18nProperties;
    }

    /**
     * Message source message source.
     *
     * @return the message source
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename(i18nProperties.getMessagesBaseName());
        log.info("18n base name : " + i18nProperties.getMessagesBaseName());
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
