package eu.isygoit.i18n.service;

import eu.isygoit.i18n.helper.LocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * The type Locale service.
 */
@Slf4j
@Service
@Transactional
public class LocaleServiceImpl implements LocaleService {

    private static final String DELIMITER = "|"; // Delimiter constant
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private LocaleResolver localeResolver;
    @Nullable
    @Autowired
    private ExtendedLocaleService extendedLocaleService;
    @Qualifier("messageMap")
    @Autowired
    private Map<String, String> messageMap;

    @Override
    public String getMessage(String code, HttpServletRequest request) {
        return getMessage(code, localeResolver.resolveLocale(request));
    }

    @Override
    public String getMessage(String code, Locale locale) {
        String message = null;
        String key = generateKey(code, locale);

        try {
            if (extendedLocaleService != null && extendedLocaleService.enabled()) {
                message = extendedLocaleService.getMessage(code, locale.toLanguageTag());
                if (!StringUtils.hasText(message)) {
                    message = getDefaultMessage(key, code, locale);
                    if (StringUtils.hasText(message)) {
                        extendedLocaleService.setMessage(code, locale.toLanguageTag(), message);
                    }
                }
            } else {
                message = getDefaultMessage(key, code, locale);
                if (StringUtils.hasText(message)) {
                    messageMap.put(key, message);
                }
            }
        } catch (Exception e) {
            log.error("<Error>: Unknown or non-translated message: {}:{} \n", locale.toLanguageTag(), code, e);
        }

        return StringUtils.hasText(message) ? message : locale.toLanguageTag() + ":" + code;
    }

    // Helper method to generate key
    private String generateKey(String code, Locale locale) {
        return code + DELIMITER + locale.toLanguageTag();
    }

    // Helper method to retrieve the message from the default sources
    private String getDefaultMessage(String key, String code, Locale locale) {
        String message = messageMap.get(key);
        if (!StringUtils.hasText(message)) {
            message = messageSource.getMessage(code, null, locale);
            if (StringUtils.hasText(message)) {
                messageMap.put(key, message);
            }
        }
        return message;
    }
}
