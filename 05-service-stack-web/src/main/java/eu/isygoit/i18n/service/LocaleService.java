package eu.isygoit.i18n.service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

/**
 * The interface Locale api.
 */
public interface LocaleService {

    /**
     * Gets message.
     *
     * @param code    the code
     * @param request the request
     * @return the message
     */
    String getMessage(String code, HttpServletRequest request);

    /**
     * Gets message.
     *
     * @param code the code
     * @param loc  the loc
     * @return the message
     */
    String getMessage(String code, Locale loc);
}
