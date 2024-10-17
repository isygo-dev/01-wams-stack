package eu.isygoit.i18n.helper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The type Locale resolver.
 */
@Slf4j
public class LocaleResolver extends AcceptHeaderLocaleResolver {

    private static final List<Locale> LOCALES = Arrays.asList(
            new Locale("en"),
            new Locale("de"),
            new Locale("fr")
    );

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String language = request.getHeader("Accept-Language");
        if (language == null || language.isEmpty()) {
            return Locale.getDefault();
        }
        List<Locale.LanguageRange> list = Locale.LanguageRange.parse(language);
        Locale locale = Locale.lookup(list, LOCALES);
        return locale;
    }
}
