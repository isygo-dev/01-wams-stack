package eu.isygoit.jwt;

import eu.isygoit.dto.common.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * The interface Jwt service.
 */
public interface IJwtService {

    /**
     * Extract domain optional.
     *
     * @param token the token
     * @return the optional
     */
    Optional<String> extractDomain(String token);

    /**
     * Extract application optional.
     *
     * @param token the token
     * @return the optional
     */
    Optional<String> extractApplication(String token);

    /**
     * Extract account type optional.
     *
     * @param token the token
     * @return the optional
     */
    Optional<String> extractAccountType(String token);

    /**
     * Extract user name optional.
     *
     * @param token the token
     * @return the optional
     */
    Optional<String> extractUserName(String token);

    /**
     * Extract subject optional.
     *
     * @param token the token
     * @param key   the key
     * @return the optional
     */
    Optional<String> extractSubject(String token, String key);

    /**
     * Extract subject optional.
     *
     * @param token the token
     * @return the optional
     */
    Optional<String> extractSubject(String token);

    /**
     * Extract expiration optional.
     *
     * @param token the token
     * @param key   the key
     * @return the optional
     */
    Optional<Date> extractExpiration(String token, String key);

    /**
     * Extract claim optional.
     *
     * @param <T>            the type parameter
     * @param token          the token
     * @param claimsResolver the claims resolver
     * @param key            the key
     * @return the optional
     */
    <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver, String key);

    /**
     * Extract claim optional.
     *
     * @param <T>            the type parameter
     * @param token          the token
     * @param claimsResolver the claims resolver
     * @return the optional
     */
    <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver);

    /**
     * Extract all claims optional.
     *
     * @param token the token
     * @param key   the key
     * @return the optional
     */
    Optional<Claims> extractAllClaims(String token, String key);

    /**
     * Extract all claims optional.
     *
     * @param token the token
     * @return the optional
     */
    Optional<Claims> extractAllClaims(String token);

    /**
     * Is token expired boolean.
     *
     * @param token the token
     * @param key   the key
     * @return the boolean
     */
    Boolean isTokenExpired(String token, String key);

    /**
     * Create token token dto.
     *
     * @param subject      the subject
     * @param claims       the claims
     * @param issuer       the issuer
     * @param audience     the audience
     * @param algorithm    the algorithm
     * @param key          the key
     * @param lifeTimeInMs the life time in ms
     * @return the token dto
     */
    TokenDto createToken(String subject, Map<String, Object> claims, String issuer, String audience
            , SignatureAlgorithm algorithm, String key, Integer lifeTimeInMs);

    /**
     * Validate token.
     *
     * @param token   the token
     * @param subject the subject
     * @param key     the key
     */
    void validateToken(String token, String subject, String key);

    /**
     * Calc expiry date date.
     *
     * @param lifeTimeInMs the life time in ms
     * @return the date
     */
    Date calcExpiryDate(Integer lifeTimeInMs);

    /**
     * Extract is admin boolean.
     *
     * @param token the token
     * @return the boolean
     */
    Boolean extractIsAdmin(String token);
}
