package eu.isygoit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.isygoit.dto.common.TokenDto;
import eu.isygoit.enums.IEnumToken;
import eu.isygoit.jwt.IJwtService;

import java.util.List;
import java.util.Map;

/**
 * The interface Token api.
 */
public interface ITokenService extends IJwtService {

    /**
     * Build token and save token dto.
     *
     * @param tenant      the tenant
     * @param application the application
     * @param tokenType   the token type
     * @param subject     the subject
     * @param claims      the claims
     * @return the token dto
     */
    TokenDto buildTokenAndSave(String tenant, String application, IEnumToken.Types tokenType, String subject, Map<String, Object> claims);

    /**
     * Build token token dto.
     *
     * @param tenant      the tenant
     * @param application the application
     * @param tokenType   the token type
     * @param subject     the subject
     * @param claims      the claims
     * @return the token dto
     */
    TokenDto buildToken(String tenant, String application, IEnumToken.Types tokenType, String subject, Map<String, Object> claims);

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


    /**
     * Create access token token dto.
     *
     * @param tenant      the tenant
     * @param application the application
     * @param userName    the user name
     * @param isAdmin     the is admin
     * @return the token dto
     */
    TokenDto createAccessToken(String tenant, String application, String userName, Boolean isAdmin);

    /**
     * Create refresh token token dto.
     *
     * @param tenant      the tenant
     * @param application the application
     * @param userName    the user name
     * @return the token dto
     */
    TokenDto createRefreshToken(String tenant, String application, String userName);

    /**
     * Create authority token token dto.
     *
     * @param tenant      the tenant
     * @param application the application
     * @param userName    the user name
     * @param authorities the authorities
     * @return the token dto
     */
    TokenDto createAuthorityToken(String tenant, String application, String userName, List<String> authorities);


    /**
     * Create forgot password access token.
     *
     * @param tenant      the tenant
     * @param application the application
     * @param accountCode the account code
     * @throws JsonProcessingException the json processing exception
     */
    void createForgotPasswordAccessToken(String tenant, String application, String accountCode) throws JsonProcessingException;
}
