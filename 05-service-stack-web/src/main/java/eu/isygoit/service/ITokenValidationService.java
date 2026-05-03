package eu.isygoit.service;

import eu.isygoit.enums.IEnumToken;

/**
 * The interface Token api.
 */
public interface ITokenValidationService {

    /**
     * Is token valid boolean.
     *
     * @param tenant      the tenant
     * @param application the application
     * @param tokenType   the token type
     * @param token       the token
     * @param subject     the subject
     * @return the boolean
     */
    boolean isTokenValid(String tenant, String application, IEnumToken.Types tokenType, String token, String subject);
}
