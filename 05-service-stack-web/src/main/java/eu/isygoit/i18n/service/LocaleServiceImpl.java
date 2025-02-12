package eu.isygoit.i18n.service;

import eu.isygoit.i18n.helper.LocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public String getMessage(String code, Locale loc) {
        String message = null;
        try {
            if (extendedLocaleService != null && extendedLocaleService.enabled()) {
                message = extendedLocaleService.getMessage(code, loc.toLanguageTag());
                if (!StringUtils.hasText(message)) {
                    message = messageMap.get(code + "|" + loc.toLanguageTag());
                    if (!StringUtils.hasText(message)) {
                        message = messageSource.getMessage(code, null, loc);
                        if (StringUtils.hasText(message)) {
                            extendedLocaleService.setMessage(code, loc.toLanguageTag(), message);
                        }
                    }
                }
            } else {
                message = messageMap.get(code + "|" + loc.toLanguageTag());
                if (!StringUtils.hasText(message)) {
                    message = messageSource.getMessage(code, null, loc);
                    if (StringUtils.hasText(message)) {
                        messageMap.put(code + "|" + loc.toLanguageTag(), message);
                    }
                }
            }
        } catch (Throwable e) {
            log.error("<Error>: unknown or Non translated message: {}:{} \n", loc.toLanguageTag(), code, e);
        }

        return StringUtils.hasText(message) ? message : loc.toLanguageTag() + ":" + code;
    }
}
