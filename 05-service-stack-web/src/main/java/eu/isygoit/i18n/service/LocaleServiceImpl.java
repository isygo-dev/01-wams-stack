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
import java.util.Optional;

/**
 * The LocaleService implementation that provides internationalization support.
 * This service retrieves localized messages based on the current locale.
 */
@Slf4j
@Service
@Transactional
public class LocaleServiceImpl implements LocaleService {

    private static final String DELIMITER = "|"; // A constant used to separate message keys

    // Autowired Spring components
    @Autowired
    private MessageSource messageSource; // Source for retrieving messages
    @Autowired
    private LocaleResolver localeResolver; // Resolves the current locale from the request
    @Nullable
    @Autowired
    private ExtendedLocaleService extendedLocaleService; // Optional extended service for custom translations
    @Qualifier("messageMap")
    @Autowired
    private Map<String, String> messageMap; // Cache to store messages to avoid repeated lookups

    /**
     * Retrieves the message for the given code and request, resolving the locale from the request.
     *
     * @param code The message code.
     * @param request The HTTP request used to resolve the locale.
     * @return The localized message.
     */
    @Override
    public String getMessage(String code, HttpServletRequest request) {
        return getMessage(code, localeResolver.resolveLocale(request)); // Resolving locale from request
    }

    /**
     * Retrieves the message for the given code and locale.
     *
     * @param code The message code.
     * @param locale The locale used to fetch the message.
     * @return The localized message.
     */
    @Override
    public String getMessage(String code, Locale locale) {
        // Generate a cache key based on code and locale
        var key = generateKey(code, locale);

        // Fetch the localized message
        var message = fetchMessage(code, locale, key);

        // Return the message if found; otherwise, return a fallback value
        return StringUtils.hasText(message) ? message : locale.toLanguageTag() + ":" + code;
    }

    /**
     * Generates a unique cache key based on the message code and locale.
     *
     * @param code The message code.
     * @param locale The locale.
     * @return The cache key.
     */
    private String generateKey(String code, Locale locale) {
        return code + DELIMITER + locale.toLanguageTag(); // Concatenate code and locale for uniqueness
    }

    /**
     * Fetches the message from either the extended locale service (if enabled) or the default service.
     *
     * @param code The message code.
     * @param locale The locale.
     * @param key The cache key.
     * @return The localized message.
     */
    private String fetchMessage(String code, Locale locale, String key) {
        try {
            // Try fetching the message using the extended locale service if available and enabled
            var message = Optional.ofNullable(extendedLocaleService)
                    .filter(ExtendedLocaleService::enabled) // Check if extended service is enabled
                    .map(service -> fetchFromExtendedService(code, locale, key, service)) // Fetch from extended service
                    .orElseGet(() -> fetchFromDefaultService(key, code, locale)); // Otherwise, fallback to the default service

            return message;
        } catch (Exception e) {
            // Log any error that occurs while fetching the message
            log.error("Error: Unknown or non-translated message: {}:{} \n", locale.toLanguageTag(), code, e);
            return null; // Return null if an error occurs
        }
    }

    /**
     * Fetches the message from the extended locale service if it's available.
     *
     * @param code The message code.
     * @param locale The locale.
     * @param key The cache key.
     * @param service The extended locale service.
     * @return The localized message from the extended service.
     */
    private String fetchFromExtendedService(String code, Locale locale, String key, ExtendedLocaleService service) {
        // Try to get the message from the extended service
        var message = service.getMessage(code, locale.toLanguageTag());

        // If the message is found, return it. Otherwise, try fetching from the default service.
        if (StringUtils.hasText(message)) {
            return message;
        } else {
            // Fetch from the default service if no message is found
            message = getDefaultMessage(key, code, locale);
            // If a message is found in the default service, store it in the extended service for future use
            if (StringUtils.hasText(message)) {
                service.setMessage(code, locale.toLanguageTag(), message);
            }
            return message;
        }
    }

    /**
     * Fetches the message from the default sources (messageMap or MessageSource).
     *
     * @param key The cache key.
     * @param code The message code.
     * @param locale The locale.
     * @return The localized message.
     */
    private String fetchFromDefaultService(String key, String code, Locale locale) {
        // First, try fetching from the cache (messageMap)
        var message = getDefaultMessage(key, code, locale);

        // If a message was found, store it in the cache for future use
        if (StringUtils.hasText(message)) {
            messageMap.put(key, message);
        }

        return message; // Return the message (null if not found)
    }

    /**
     * Retrieves the default message either from the cache or the MessageSource.
     *
     * @param key The cache key.
     * @param code The message code.
     * @param locale The locale.
     * @return The localized message.
     */
    private String getDefaultMessage(String key, String code, Locale locale) {
        // Try fetching the message from the cache (messageMap)
        var message = messageMap.get(key);
        if (!StringUtils.hasText(message)) {
            // If not found, fetch from the message source
            message = messageSource.getMessage(code, null, locale);
            // Store the fetched message in the cache for future lookups
            if (StringUtils.hasText(message)) {
                messageMap.put(key, message);
            }
        }
        return message;
    }
}