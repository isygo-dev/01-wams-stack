package eu.isygoit.jwt;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.TokenResponseDto;
import eu.isygoit.enums.IEnumWebToken;
import eu.isygoit.exception.TokenInvalidException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

/**
 * Service for managing JWT tokens (JJWT 0.13.0+ compatible).
 * Provides **both secured and unsecured** extraction methods.
 */
@Slf4j
@Service
@Transactional
public class JwtService implements IJwtService {

    public static final String AUTHORIZATION = "Authorization";

    // ========================================================================
    // UNSECURED convenience extractors (no key)
    // ========================================================================

    @Override
    public Optional<String> extractTenant(String token) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting tenant from unsigned token");
        return extractClaim(token, JwtConstants.JWT_SENDER_TENANT, String.class);
    }

    @Override
    public Boolean extractIsAdmin(String token) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting isAdmin flag from unsigned token");
        return extractClaim(token, JwtConstants.JWT_IS_ADMIN, Boolean.class)
                .map(Boolean.class::cast)
                .orElse(Boolean.FALSE);
    }

    @Override
    public Optional<String> extractApplication(String token) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting application from unsigned token");
        return extractClaim(token, JwtConstants.JWT_LOG_APP, String.class);
    }

    @Override
    public Optional<String> extractAccountType(String token) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting account type from unsigned token");
        return extractClaim(token, JwtConstants.JWT_SENDER_ACCOUNT_TYPE, String.class);
    }

    @Override
    public Optional<String> extractUserName(String token) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting username from unsigned token");
        return extractClaim(token, JwtConstants.JWT_SENDER_USER, String.class);
    }

    @Override
    public Optional<String> extractSubject(String token) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting subject from unsigned token");
        return extractClaim(token, Claims::getSubject);
    }

    // ========================================================================
    // SECURED convenience extractors (with key)
    // ========================================================================

    @Override
    public Optional<String> extractTenant(String token, String key) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting tenant from signed token");
        return extractClaim(token, JwtConstants.JWT_SENDER_TENANT, String.class, key);
    }

    @Override
    public Boolean extractIsAdmin(String token, String key) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting isAdmin flag from signed token");
        return extractClaim(token, JwtConstants.JWT_IS_ADMIN, Boolean.class, key)
                .map(Boolean.class::cast)
                .orElse(Boolean.FALSE);
    }

    @Override
    public Optional<String> extractApplication(String token, String key) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting application from signed token");
        return extractClaim(token, JwtConstants.JWT_LOG_APP, String.class, key);
    }

    @Override
    public Optional<String> extractAccountType(String token, String key) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting account type from signed token");
        return extractClaim(token, JwtConstants.JWT_SENDER_ACCOUNT_TYPE, String.class, key);
    }

    @Override
    public Optional<String> extractUserName(String token, String key) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting username from signed token");
        return extractClaim(token, JwtConstants.JWT_SENDER_USER, String.class, key);
    }

    @Override
    public Optional<String> extractSubject(String token, String key) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting subject from signed token");
        return extractClaim(token, Claims::getSubject, key);
    }

    // ========================================================================
    // Generic claim extractors
    // ========================================================================

    public <T> Optional<T> extractClaim(String token, String claimKey, Class<T> claimClass) {
        Claims claims = extractAllClaims(token);
        return Optional.ofNullable(claims.get(claimKey, claimClass));
    }

    public <T> Optional<T> extractClaim(String token, String claimKey, Class<T> claimClass, String key) {
        Claims claims = extractAllClaims(token, key);
        return Optional.ofNullable(claims.get(claimKey, claimClass));
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver) {
        return Optional.ofNullable(claimsResolver.apply(extractAllClaims(token)));
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver, String key) {
        return Optional.ofNullable(claimsResolver.apply(extractAllClaims(token, key)));
    }

    // ========================================================================
    // Core claim extraction (JJWT 0.13.0 safe)
    // ========================================================================

    @Override
    public Claims extractAllClaims(String token) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting all claims (unsigned) from token");
        try {
            String[] parts = token.split("\\.", 3);
            if (parts.length < 2) throw new IllegalArgumentException("Invalid JWT format");

            String unsecuredHeader = "eyJhbGciOiJub25lIn0"; // {"alg":"none"}
            String payload = parts[1];
            String unsecuredToken = unsecuredHeader + "." + payload + ".";

            return Jwts.parser()
                    .unsecured()
                    .build()
                    .parseUnsecuredClaims(unsecuredToken)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Failed to parse unsigned claims: {}", ex.getMessage());
            throw new TokenInvalidException("Failed to parse JWT claims", ex);
        }
    }

    @Override
    public Claims extractAllClaims(String token, String key) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting all claims from signed token");
        try {
            return Jwts.parser()
                    .verifyWith(buildSecretKey(key))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            log.error("JWT expired: {}", ex.getMessage());
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Failed to parse signed claims: {}", ex.getMessage());
            throw new TokenInvalidException("Failed to parse JWT claims with signing key", ex);
        }
    }

    // ========================================================================
    // Token creation / validation / utilities
    // ========================================================================

    @Override
    public TokenResponseDto createToken(String subject, Map<String, Object> claims, String issuer, String audience,
                                        MacAlgorithm algorithm, String key, Integer lifeTimeInMs) {
        return createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(claims)
                .issuer(issuer)
                .audience(audience)
                .algorithm(algorithm)
                .key(key)
                .lifeTimeInMs(lifeTimeInMs)
                .build());
    }

    @Override
    public TokenResponseDto createToken(JwtTokenRequest request) {
        log.info("Creating new JWT token for subject: {}", request.subject());
        Date expiryDate = calcExpiryDate(request.lifeTimeInMs());
        SecretKey secretKey = buildSecretKey(request.key());

        JwtBuilder jwtBuilder = Jwts.builder()
                .subject(request.subject())
                .issuer(request.issuer())
                .issuedAt(Date.from(Instant.now()))
                .expiration(expiryDate)
                .audience().add(request.audience()).and()
                .signWith(secretKey, request.algorithm());

        if (!CollectionUtils.isEmpty(request.claims())) {
            request.claims().forEach(jwtBuilder::claim);
        }

        String token = jwtBuilder.compact();
        log.info("JWT token created successfully for subject: {}", request.subject());
        return new TokenResponseDto(IEnumWebToken.Types.Bearer, token, expiryDate);
    }

    @Override
    public void validateToken(String token, String subject, String key) {
        log.info("Validating JWT token for subject: {}", subject);
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("token cannot be null or empty");
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(buildSecretKey(key))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenSubject = claims.getSubject();
            if (!StringUtils.hasText(tokenSubject) || !tokenSubject.equalsIgnoreCase(subject)) {
                throw new TokenInvalidException("Invalid JWT: subject does not match");
            }
        } catch (TokenInvalidException ex) {
            throw ex;
        } catch (SecurityException ex) {
            throw new TokenInvalidException("Invalid JWT: signature", ex);
        } catch (MalformedJwtException ex) {
            throw new TokenInvalidException("Invalid JWT: malformed", ex);
        } catch (ExpiredJwtException ex) {
            throw new TokenInvalidException("Invalid JWT: expired", ex);
        } catch (UnsupportedJwtException ex) {
            throw new TokenInvalidException("Invalid JWT: unsupported", ex);
        } catch (IllegalArgumentException ex) {
            throw new TokenInvalidException("Invalid JWT: illegal argument", ex);
        }
    }

    @Override
    public Boolean isTokenExpired(String token, String key) {
        return extractExpiration(token, key)
                .map(exp -> exp.before(Date.from(Instant.now())))
                .orElse(true);
    }

    @Override
    public Optional<Date> extractExpiration(String token, String key) {
        if (!StringUtils.hasText(token)) throw new IllegalArgumentException("token cannot be null or empty");
        log.debug("Extracting expiration date from signed token");
        return extractClaim(token, Claims::getExpiration, key);
    }

    @Override
    public Date calcExpiryDate(Integer lifeTimeInMs) {
        return Date.from(Instant.now().plusMillis(lifeTimeInMs));
    }

    private SecretKey buildSecretKey(String key) {
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }
}