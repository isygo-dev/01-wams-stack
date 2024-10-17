package eu.isygoit.i18n.service;

/**
 * The interface Extended locale service.
 */
public interface ExtendedLocaleService {

    /**
     * Load message string.
     *
     * @param code   the code
     * @param locale the locale
     * @return the string
     */
    String loadMessage(String code, String locale);

    /**
     * Gets message.
     *
     * @param code   the code
     * @param locale the locale
     * @return the message
     */
    String getMessage(String code, String locale);

    /**
     * Sets message.
     *
     * @param code    the code
     * @param locale  the locale
     * @param message the message
     */
    void setMessage(String code, String locale, String message);

    /**
     * Clear.
     */
    void clear();

    /**
     * Refresh.
     */
    void refresh();

    /**
     * Enabled boolean.
     *
     * @return the boolean
     */
    boolean enabled();
}
