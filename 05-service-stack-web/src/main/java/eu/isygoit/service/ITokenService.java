package eu.isygoit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.isygoit.dto.common.TokenDto;
import eu.isygoit.enums.IEnumToken;
import eu.isygoit.jwt.IJwtService;

import java.util.List;
import java.util.Map;

/**
 * The interface Token service.
 */
public interface ITokenService extends IJwtService {

    /**
     * Build token and save token dto.
     *
     * @param domain      the domain
     * @param application the application
     * @param tokenType   the token type
     * @param subject     the subject
     * @param claims      the claims
     * @return the token dto
     */
    TokenDto buildTokenAndSave(String domain, String application, IEnumToken.Types tokenType, String subject, Map<String, Object> claims);

    /**
     * Build token token dto.
     *
     * @param domain      the domain
     * @param application the application
     * @param tokenType   the token type
     * @param subject     the subject
     * @param claims      the claims
     * @return the token dto
     */
    TokenDto buildToken(String domain, String application, IEnumToken.Types tokenType, String subject, Map<String, Object> claims);

    /**
     * Is token valid boolean.
     *
     * @param domain      the domain
     * @param application the application
     * @param tokenType   the token type
     * @param token       the token
     * @param subject     the subject
     * @return the boolean
     */
    boolean isTokenValid(String domain, String application, IEnumToken.Types tokenType, String token, String subject);


    /**
     * Create access token token dto.
     *
     * @param domain      the domain
     * @param application the application
     * @param userName    the user name
     * @param isAdmin     the is admin
     * @return the token dto
     */
    TokenDto createAccessToken(String domain, String application, String userName, Boolean isAdmin);

    /**
     * Create refresh token token dto.
     *
     * @param domain      the domain
     * @param application the application
     * @param userName    the user name
     * @return the token dto
     */
    TokenDto createRefreshToken(String domain, String application, String userName);

    /**
     * Create authority token token dto.
     *
     * @param domain      the domain
     * @param application the application
     * @param userName    the user name
     * @param authorities the authorities
     * @return the token dto
     */
    TokenDto createAuthorityToken(String domain, String application, String userName, List<String> authorities);


    /**
     * Create forgot password access token.
     *
     * @param domain      the domain
     * @param application the application
     * @param accountCode the account code
     * @throws JsonProcessingException the json processing exception
     */
    void createForgotPasswordAccessToken(String domain, String application, String accountCode) throws JsonProcessingException;
}
