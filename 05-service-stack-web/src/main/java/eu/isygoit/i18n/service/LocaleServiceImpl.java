package eu.isygoit.i18n.service;

import eu.isygoit.i18n.helper.LocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
 * Service pour la gestion des messages internationaux.
 */
@Slf4j
@Service
@Transactional
public class LocaleServiceImpl implements LocaleService {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;
    private final Map<String, String> messageMap;
    private final ExtendedLocaleService extendedLocaleService;

    /**
     * Constructeur avec injection explicite des dépendances.
     *
     * @param messageSource         the message source
     * @param localeResolver        the locale resolver
     * @param messageMap            the message map
     * @param extendedLocaleService the extended locale api
     */
    public LocaleServiceImpl(
            MessageSource messageSource,
            LocaleResolver localeResolver,
            @Qualifier("messageMap") Map<String, String> messageMap,
            @Nullable ExtendedLocaleService extendedLocaleService) {
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
        this.messageMap = messageMap;
        this.extendedLocaleService = extendedLocaleService;
    }

    @Override
    public String getMessage(String code, HttpServletRequest request) {
        Locale locale = localeResolver.resolveLocale(request);
        return getMessage(code, locale);
    }

    @Override
    public String getMessage(String code, Locale locale) {
        String localeTag = locale.toLanguageTag();
        String cacheKey = code + "|" + localeTag;

        try {
            // Vérification avec ExtendedLocaleService si activé
            if (Optional.ofNullable(extendedLocaleService).map(ExtendedLocaleService::enabled).orElse(false)) {
                return getMessageFromExtendedService(code, locale, cacheKey);
            }

            // Vérification dans le cache local
            return getMessageFromCacheOrSource(code, locale, cacheKey);
        } catch (Throwable e) {
            log.error("Erreur lors de la récupération du message [{}:{}]", localeTag, code, e);
        }

        // Retourne un message par défaut si introuvable
        return localeTag + ":" + code;
    }

    /**
     * Récupère le message en utilisant ExtendedLocaleService si disponible.
     */
    private String getMessageFromExtendedService(String code, Locale locale, String cacheKey) {
        String message = extendedLocaleService.getMessage(code, locale.toLanguageTag());
        if (!StringUtils.hasText(message)) {
            message = getMessageFromCacheOrSource(code, locale, cacheKey);
            if (StringUtils.hasText(message)) {
                extendedLocaleService.setMessage(code, locale.toLanguageTag(), message);
            }
        }
        return message;
    }

    /**
     * Récupère le message depuis le cache ou la source de messages.
     */
    private String getMessageFromCacheOrSource(String code, Locale locale, String cacheKey) {
        return Optional.ofNullable(messageMap.get(cacheKey))
                .filter(StringUtils::hasText)
                .orElseGet(() -> {
                    String message = messageSource.getMessage(code, null, locale);
                    if (StringUtils.hasText(message)) {
                        messageMap.put(cacheKey, message);
                    }
                    return message;
                });
    }
}