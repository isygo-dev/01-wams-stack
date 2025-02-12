package eu.isygoit.i18n.helper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * A custom Locale resolver that extends the AcceptHeaderLocaleResolver.
 * This resolver attempts to match the client's preferred language from the "Accept-Language" header
 * against a predefined list of supported locales.
 * If no match is found, it defaults to the system's default locale.
 */
@Slf4j
public class LocaleResolver extends AcceptHeaderLocaleResolver {

    // Optimized list creation using List.of(), which creates an immutable list
    private static final List<Locale> SUPPORTED_LOCALES = List.of(
            new Locale("en"),
            new Locale("de"),
            new Locale("fr")
    );

    /**
     * Resolves the locale from the "Accept-Language" header of the request.
     * If the header is empty or the language cannot be determined, the default locale is returned.
     *
     * @param request The HTTP request containing the "Accept-Language" header.
     * @return The resolved locale.
     */
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // Get the Accept-Language header
        var languageHeader = Optional.ofNullable(request.getHeader("Accept-Language"));

        // If header is present and non-empty, try to resolve the locale
        var resolvedLocale = languageHeader
                .filter(lang -> !lang.isEmpty()) // Only proceed if the header is not empty
                .map(lang -> {
                    // Parse the language range and find the best match from supported locales
                    var languageRanges = Locale.LanguageRange.parse(lang);
                    return Locale.lookup(languageRanges, SUPPORTED_LOCALES);
                })
                // If no match is found, use the default locale
                .orElse(Locale.getDefault());

        // Log the resolved locale
        log.info("Resolved locale: {}", resolvedLocale);

        return resolvedLocale;
    }
}