package eu.isygoit.i18n.service;

import eu.isygoit.model.extendable.LocaleMessageModel;
import eu.isygoit.repository.MessageModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * The type Abstract extended locale service.
 */
public abstract class AbstractExtendedLocaleService implements ExtendedLocaleService {

    private static final String DELIMITER = "|"; // Using a constant for delimiter
    @Qualifier("extendedMessageMap")
    @Autowired
    private Map<String, String> extendedMessageMap;

    @Override
    public final String getMessage(String code, String locale) {
        String key = generateKey(code, locale);
        String message = extendedMessageMap.get(key);

        if (StringUtils.hasText(message)) {
            return message;
        }

        message = loadMessage(code, locale);
        if (StringUtils.hasText(message)) {
            extendedMessageMap.put(key, message);
        }

        return message;
    }

    @Override
    public final void clear() {
        extendedMessageMap.clear();
    }

    @Override
    public final void refresh() {
        extendedMessageMap.forEach((code, message) -> {
            String[] codePlisLocal = code.split("\\|"); // Fix regex issue
            loadMessage(codePlisLocal[0], codePlisLocal[1]);
        });
    }

    @Override
    public String loadMessage(String code, String locale) {
        Optional<LocaleMessageModel> optionalMessage = getMessageRepository().findByCodeIgnoreCaseAndLocale(code, locale);
        return optionalMessage.map(LocaleMessageModel::getText).orElse(null); // Cleaner way to handle Optional
    }

    @Override
    public void setMessage(String code, String locale, String message) {
        String key = generateKey(code, locale);
        extendedMessageMap.put(key, message);

        Optional<LocaleMessageModel> optional = getMessageRepository().findByCodeIgnoreCaseAndLocale(code, locale);
        optional.ifPresentOrElse(
                localeMessageModel -> {
                    localeMessageModel.setText(message);
                    getMessageRepository().save(localeMessageModel);
                },
                () -> getMessageRepository().save(LocaleMessageModel.builder()
                        .code(code)
                        .locale(locale)
                        .text(message)
                        .build())
        );
    }

    private String generateKey(String code, String locale) {
        return String.format("%s%s%s", code, DELIMITER, locale); // Using String.format for clarity
    }

    /**
     * Gets message repository.
     *
     * @return the message repository
     */
    public abstract MessageModelRepository getMessageRepository();

    @Override
    public boolean enabled() {
        return true;
    }
}