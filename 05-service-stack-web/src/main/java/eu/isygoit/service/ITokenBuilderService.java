package eu.isygoit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.isygoit.dto.common.TokenResponseDto;
import eu.isygoit.enums.IEnumToken;

import java.util.List;
import java.util.Map;

/**
 * The interface Token api.
 */
public interface ITokenBuilderService extends ITokenValidationService {

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
    TokenResponseDto buildTokenAndSave(String tenant, String application, IEnumToken.Types tokenType, String subject, Map<String, Object> claims);

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
    TokenResponseDto buildToken(String tenant, String application, IEnumToken.Types tokenType, String subject, Map<String, Object> claims);

    /**
     * Create access token token dto.
     *
     * @param tenant      the tenant
     * @param application the application
     * @param userName    the user name
     * @param isAdmin     the is admin
     * @return the token dto
     */
    TokenResponseDto buildAccessToken(String tenant, String application, String userName, Boolean isAdmin);

    /**
     * Create refresh token token dto.
     *
     * @param tenant      the tenant
     * @param application the application
     * @param userName    the user name
     * @return the token dto
     */
    TokenResponseDto buildRefreshToken(String tenant, String application, String userName);

    /**
     * Create authority token token dto.
     *
     * @param tenant      the tenant
     * @param application the application
     * @param userName    the user name
     * @param authorities the authorities
     * @return the token dto
     */
    TokenResponseDto buildAuthorityToken(String tenant, String application, String userName, List<String> authorities);


    /**
     * Create forgot password access token.
     *
     * @param tenant      the tenant
     * @param application the application
     * @param accountCode the account code
     * @throws JsonProcessingException the json processing exception
     */
    void buildForgotPasswordAccessToken(String tenant, String application, String accountCode) throws JsonProcessingException;
}
