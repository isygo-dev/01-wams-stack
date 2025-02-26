package eu.isygoit.i18n.service;

import eu.isygoit.model.extendable.LocaleMessageModel;
import eu.isygoit.repository.MessageModelRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * Service abstrait pour la gestion des messages étendus.
 */
public abstract class AbstractExtendedLocaleService implements ExtendedLocaleService {

    private final Map<String, String> extendedMessageMap;

    /**
     * Constructeur avec injection explicite du cache.
     *
     * @param extendedMessageMap the extended message map
     */
    protected AbstractExtendedLocaleService(@Qualifier("extendedMessageMap") Map<String, String> extendedMessageMap) {
        this.extendedMessageMap = extendedMessageMap;
    }

    @Override
    public final String getMessage(String code, String locale) {
        String cacheKey = code + "|" + locale;

        return Optional.ofNullable(extendedMessageMap.get(cacheKey))
                .filter(StringUtils::hasText)
                .orElseGet(() -> {
                    String message = loadMessage(code, locale);
                    if (StringUtils.hasText(message)) {
                        extendedMessageMap.put(cacheKey, message);
                    }
                    return message;
                });
    }

    @Override
    public final void clear() {
        extendedMessageMap.clear();
    }

    @Override
    public final void refresh() {
        extendedMessageMap.replaceAll((code, oldMessage) -> {
            String[] parts = code.split("\\|");
            if (parts.length == 2) {
                return loadMessage(parts[0], parts[1]);
            }
            return oldMessage; // Conserve l'ancienne valeur en cas d'erreur
        });
    }

    @Override
    public String loadMessage(String code, String locale) {
        Optional<LocaleMessageModel> optionalMessage = getMessageRepository().findByCodeIgnoreCaseAndLocale(code, locale);
        if (optionalMessage.isPresent()) {
            return optionalMessage.get().getText();
        }
        return null;
    }

    @Override
    public void setMessage(String code, String locale, String message) {
        String cacheKey = code + "|" + locale;

        // Mettez à jour le cache
        extendedMessageMap.put(cacheKey, message);

        // Chercher le message dans la base de données
        Optional<LocaleMessageModel> optionalMessage = getMessageRepository()
                .findByCodeIgnoreCaseAndLocale(code, locale);

        // Si le message existe, mettez à jour, sinon créez un nouveau message
        optionalMessage.ifPresentOrElse(
                existingMessage -> {
                    existingMessage.setText(message);
                    getMessageRepository().save(existingMessage);
                },
                () -> getMessageRepository().save(LocaleMessageModel.builder()
                        .code(code)
                        .locale(locale)
                        .text(message)
                        .build())
        );
    }

    /**
     * Obtient le référentiel des messages.
     *
     * @return le repository utilisé pour récupérer et stocker les messages.
     */
    public abstract MessageModelRepository getMessageRepository();

    @Override
    public boolean enabled() {
        return true;
    }
}