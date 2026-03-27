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
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Service for managing JWT tokens.
 * Provides methods to create, validate, and extract information from JWTs.
 *
 * <p>Migrated from JJWT 0.9.x to 0.12.x — key API changes:
 * <ul>
 *   <li>{@code .setSigningKey(String)} → {@code .verifyWith(SecretKey)}</li>
 *   <li>{@code .parseClaimsJws(token)} → {@code .parseSignedClaims(token)}</li>
 *   <li>{@code .parseClaimsJwt(token)} → {@code .parseUnsecuredClaims(token)}</li>
 *   <li>{@code .getBody()} → {@code .getPayload()}</li>
 *   <li>{@code .setSubject()} → {@code .subject()}</li>
 *   <li>{@code .setIssuer()} → {@code .issuer()}</li>
 *   <li>{@code .setIssuedAt()} → {@code .issuedAt()}</li>
 *   <li>{@code .setExpiration()} → {@code .expiration()}</li>
 *   <li>{@code .setAudience()} → {@code .audience().add().and()}</li>
 *   <li>{@code .signWith(algorithm, key)} → {@code .signWith(secretKey, algorithm)}</li>
 *   <li>{@code SignatureAlgorithm} enum → {@code MacAlgorithm} / {@code Jwts.SIG.*}</li>
 *   <li>{@code io.jsonwebtoken.SignatureException} → {@code io.jsonwebtoken.security.SecurityException}</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional
public class JwtService implements IJwtService {

    /**
     * HTTP Authorization header name.
     */
    public static final String AUTHORIZATION = "Authorization";

    // ─────────────────────────────────────────────────────────────────────────
    // Subject / Claim extractors (unsigned — no key required)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Optional<String> extractSubject(String token) {
        log.debug("Extracting subject from token");
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Optional<String> extractTenant(String token) {
        log.debug("Extracting tenant from token");
        return extractClaim(token, JwtConstants.JWT_SENDER_TENANT, String.class);
    }

    @Override
    public Boolean extractIsAdmin(String token) {
        log.debug("Extracting isAdmin flag from token");
        return extractClaim(token, JwtConstants.JWT_IS_ADMIN, Boolean.class)
                .map(Boolean.class::cast)
                .orElse(Boolean.FALSE);
    }

    @Override
    public Optional<String> extractApplication(String token) {
        log.debug("Extracting application from token");
        return extractClaim(token, JwtConstants.JWT_LOG_APP, String.class);
    }

    @Override
    public Optional<String> extractAccountType(String token) {
        log.debug("Extracting account type from token");
        return extractClaim(token, JwtConstants.JWT_SENDER_ACCOUNT_TYPE, String.class);
    }

    @Override
    public Optional<String> extractUserName(String token) {
        log.debug("Extracting username from token");
        return extractClaim(token, JwtConstants.JWT_SENDER_USER, String.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Subject / Claim extractors (signed — key required)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Optional<String> extractSubject(String token, String key) {
        log.debug("Extracting subject from signed token");
        return extractClaim(token, Claims::getSubject, key);
    }

    @Override
    public Optional<Date> extractExpiration(String token, String key) {
        log.debug("Extracting expiration date from signed token");
        return extractClaim(token, Claims::getExpiration, key);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Generic claim extractors
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extracts a named claim from an unsigned token by key and expected type.
     *
     * @param <T>        type of the claim value
     * @param token      JWT token string (signature not verified)
     * @param claimKey   claim key to look up
     * @param claimClass expected class of the claim value
     * @return Optional containing the claim value if present
     */
    public <T> Optional<T> extractClaim(String token, String claimKey, Class<T> claimClass) {
        log.debug("Extracting claim '{}' from unsigned token", claimKey);
        Claims claims = extractAllClaims(token);
        return Optional.ofNullable(claims.get(claimKey, claimClass));
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.debug("Extracting claim (unsigned)");
        return Optional.ofNullable(claimsResolver.apply(extractAllClaims(token)));
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver, String key) {
        log.debug("Extracting claim (signed)");
        return Optional.ofNullable(claimsResolver.apply(extractAllClaims(token, key)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Core claim extraction
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extracts all claims from a token WITHOUT verifying the signature.
     *
     * <p>Use this only for pre-inspection (e.g. reading the subject before
     * deciding which signing key to use). Never use as the sole authentication
     * check — always call {@link #validateToken} or {@link #extractAllClaims(String, String)}
     * for any security-sensitive decision.
     *
     * <p>JJWT 0.12 migration:
     * <pre>
     *   // 0.9: Jwts.parser().parseClaimsJwt(stripped).getBody()
     *   // 0.12: Jwts.parser().unsecured().build().parseUnsecuredClaims(stripped).getPayload()
     * </pre>
     *
     * @param token JWT token string
     * @return parsed {@link Claims}
     * @throws TokenInvalidException if the token cannot be parsed
     */
    public Claims extractAllClaims(String token) {
        log.debug("Extracting all claims (unsigned) from token");
        try {
            // Strip the signature — retain "header.payload." (trailing dot required)
            String unsignedToken = token.substring(0, token.lastIndexOf('.') + 1);

            return Jwts.parser()
                    .unsecured()                         // 0.12: opt-in to unsigned JWT parsing
                    .build()
                    .parseUnsecuredClaims(unsignedToken) // 0.12: replaces parseClaimsJwt
                    .getPayload();                       // 0.12: replaces getBody

        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Failed to parse unsigned claims: {}", ex.getMessage());
            throw new TokenInvalidException("Failed to parse JWT claims", ex);
        }
    }

    /**
     * Extracts all claims from a token AND verifies the signature.
     *
     * <p>JJWT 0.12 migration:
     * <pre>
     *   // 0.9: Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody()
     *   // 0.12: Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
     * </pre>
     *
     * @param token JWT token string
     * @param key   HMAC signing key (min 32 chars for HS256)
     * @return parsed and verified {@link Claims}
     * @throws ExpiredJwtException   if the token has expired
     * @throws TokenInvalidException for any other JWT parsing failure
     */
    @Override
    public Claims extractAllClaims(String token, String key) {
        log.debug("Extracting all claims from signed token");
        try {
            return Jwts.parser()
                    .verifyWith(buildSecretKey(key))  // 0.12: replaces .setSigningKey(key)
                    .build()                          // 0.12: build() now required
                    .parseSignedClaims(token)         // 0.12: replaces .parseClaimsJws(token)
                    .getPayload();                    // 0.12: replaces .getBody()

        } catch (ExpiredJwtException ex) {
            log.error("JWT expired: {}", ex.getMessage());
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Failed to parse signed claims: {}", ex.getMessage());
            throw new TokenInvalidException("Failed to parse JWT claims with signing key", ex);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Token creation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a signed JWT token.
     *
     * <p>JJWT 0.12 migration — builder API renamed to fluent style:
     * <pre>
     *   // 0.9                          // 0.12
     *   .setSubject(s)              →   .subject(s)
     *   .setIssuer(i)               →   .issuer(i)
     *   .setIssuedAt(d)             →   .issuedAt(d)
     *   .setExpiration(d)           →   .expiration(d)
     *   .setAudience(a)             →   .audience().add(a).and()
     *   .signWith(algorithm, key)   →   .signWith(secretKey, algorithm)
     * </pre>
     *
     * <p>The {@code algorithm} parameter now accepts {@link MacAlgorithm} values
     * from {@code Jwts.SIG.*} (e.g. {@code Jwts.SIG.HS256}, {@code Jwts.SIG.HS512}).
     * The legacy {@link SignatureAlgorithm} enum is deprecated in 0.12.
     *
     * @param subject      token subject (typically username or user ID)
     * @param claims       additional claims to embed
     * @param issuer       token issuer identifier
     * @param audience     intended audience
     * @param algorithm    HMAC signing algorithm (use {@code Jwts.SIG.HS256} etc.)
     * @param key          HMAC signing key string (min 32 chars for HS256)
     * @param lifeTimeInMs token validity in milliseconds
     * @return {@link TokenResponseDto} containing the token string and expiry date
     */
    @Override
    public TokenResponseDto createToken(String subject, Map<String, Object> claims, String issuer, String audience,
                                        MacAlgorithm algorithm, String key, Integer lifeTimeInMs) {

        log.info("Creating new JWT token for subject: {}", subject);
        Date expiryDate = calcExpiryDate(lifeTimeInMs);
        SecretKey secretKey = buildSecretKey(key);

        JwtBuilder jwtBuilder = Jwts.builder()
                .subject(subject)                      // 0.12: replaces .setSubject()
                .issuer(issuer)                        // 0.12: replaces .setIssuer()
                .issuedAt(Date.from(Instant.now()))    // 0.12: replaces .setIssuedAt()
                .expiration(expiryDate)                // 0.12: replaces .setExpiration()
                .audience().add(audience).and()        // 0.12: replaces .setAudience()
                .signWith(secretKey, algorithm);       // 0.12: replaces .signWith(algo, key)

        if (!CollectionUtils.isEmpty(claims)) {
            claims.forEach(jwtBuilder::claim);
        }

        String token = jwtBuilder.compact();
        log.info("JWT token created successfully for subject: {}", subject);

        return new TokenResponseDto(IEnumWebToken.Types.Bearer, token, expiryDate);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Token validation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates a signed JWT token against the expected subject and signing key.
     * Throws {@link TokenInvalidException} for any validation failure.
     *
     * @param token   JWT token string to validate
     * @param subject expected subject value to match
     * @param key     HMAC signing key
     * @throws TokenInvalidException on any validation failure
     */
    @Override
    public void validateToken(String token, String subject, String key) {
        log.info("Validating JWT token for subject: {}", subject);

        if (!StringUtils.hasText(token)) {
            log.error("Invalid JWT: token is null or empty");
            throw new TokenInvalidException("Invalid JWT token: null or empty");
        }

        try {
            // Parse and verify signature in one step — avoids parsing twice
            Claims claims = Jwts.parser()
                    .verifyWith(buildSecretKey(key))  // 0.12: replaces .setSigningKey(key)
                    .build()
                    .parseSignedClaims(token)         // 0.12: replaces .parseClaimsJws(token)
                    .getPayload();                    // 0.12: replaces .getBody()

            // Validate subject matches expected value
            String tokenSubject = claims.getSubject();
            if (!StringUtils.hasText(tokenSubject) || !tokenSubject.equalsIgnoreCase(subject)) {
                log.error("Invalid JWT: subject mismatch — expected '{}', got '{}'", subject, tokenSubject);
                throw new TokenInvalidException("Invalid JWT: subject does not match");
            }

            log.info("JWT token is valid for subject: {}", subject);

        } catch (TokenInvalidException ex) {
            // Already a domain exception — rethrow without wrapping
            throw ex;
        } catch (SecurityException ex) {
            // 0.12: io.jsonwebtoken.security.SecurityException replaces SignatureException
            log.error("Invalid JWT signature: {}", ex.getMessage());
            throw new TokenInvalidException("Invalid JWT: signature", ex);
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT format: {}", ex.getMessage());
            throw new TokenInvalidException("Invalid JWT: malformed", ex);
        } catch (ExpiredJwtException ex) {
            log.error("JWT token has expired: {}", ex.getMessage());
            throw new TokenInvalidException("Invalid JWT: expired", ex);
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
            throw new TokenInvalidException("Invalid JWT: unsupported", ex);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty or illegal: {}", ex.getMessage());
            throw new TokenInvalidException("Invalid JWT: illegal argument", ex);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Boolean isTokenExpired(String token, String key) {
        log.debug("Checking if token is expired");
        return extractExpiration(token, key)
                .map(exp -> exp.before(Date.from(Instant.now())))
                .orElse(true); // treat as expired if expiration claim is missing
    }

    @Override
    public Date calcExpiryDate(Integer lifeTimeInMs) {
        log.debug("Calculating expiration date for lifetime: {}ms", lifeTimeInMs);
        return Date.from(Instant.now().plusMillis(lifeTimeInMs));
    }

    /**
     * Builds a {@link SecretKey} from a raw string for HMAC signing/verification.
     *
     * <p>Key length requirements:
     * <ul>
     *   <li>HS256 — minimum 32 characters (256 bits)</li>
     *   <li>HS384 — minimum 48 characters (384 bits)</li>
     *   <li>HS512 — minimum 64 characters (512 bits)</li>
     * </ul>
     * A {@code WeakKeyException} is thrown at runtime if the key is too short.
     *
     * @param key raw signing key string
     * @return {@link SecretKey} suitable for {@code verifyWith()} and {@code signWith()}
     */
    private SecretKey buildSecretKey(String key) {
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }
}