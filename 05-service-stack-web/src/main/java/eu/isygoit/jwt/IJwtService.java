package eu.isygoit.jwt;

import eu.isygoit.dto.common.TokenResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.MacAlgorithm;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * The interface Jwt api.
 * <p>
 * Supports **both secured and unsecured** extraction for every claim.
 */
public interface IJwtService {

    // ─────────────────────────────────────────────────────────────────────────
    // Unsecured (no key) — fast pre-inspection
    // ─────────────────────────────────────────────────────────────────────────
    Optional<String> extractTenant(String token);

    Optional<String> extractApplication(String token);

    Optional<String> extractAccountType(String token);

    Optional<String> extractUserName(String token);

    Boolean extractIsAdmin(String token);

    Optional<String> extractSubject(String token);

    // ─────────────────────────────────────────────────────────────────────────
    // Secured (with key) — signature verified
    // ─────────────────────────────────────────────────────────────────────────
    Optional<String> extractTenant(String token, String key);

    Optional<String> extractApplication(String token, String key);

    Optional<String> extractAccountType(String token, String key);

    Optional<String> extractUserName(String token, String key);

    Boolean extractIsAdmin(String token, String key);

    Optional<String> extractSubject(String token, String key);

    // ─────────────────────────────────────────────────────────────────────────
    // Other signed-only methods
    // ─────────────────────────────────────────────────────────────────────────
    Optional<Date> extractExpiration(String token, String key);

    <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver, String key);

    <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver);

    Claims extractAllClaims(String token, String key);

    Claims extractAllClaims(String token);

    // ─────────────────────────────────────────────────────────────────────────
    // Core API
    // ─────────────────────────────────────────────────────────────────────────
    Boolean isTokenExpired(String token, String key);

    TokenResponseDto createToken(String subject, Map<String, Object> claims, String issuer, String audience,
                                 MacAlgorithm algorithm, String key, Integer lifeTimeInMs);

    void validateToken(String token, String subject, String key);

    Date calcExpiryDate(Integer lifeTimeInMs);
}