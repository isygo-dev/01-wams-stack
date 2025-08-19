package eu.isygoit.jwt;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.TokenResponseDto;
import eu.isygoit.enums.IEnumWebToken;
import eu.isygoit.exception.TokenInvalidException;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Service for managing JWT tokens.
 * Provides methods to create, validate, and extract information from JWTs.
 */
@Slf4j
@Service
@Transactional
public class JwtService implements IJwtService {

    /**
     * The constant AUTHORIZATION.
     */
    public static final String AUTHORIZATION = "Authorization";

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

    @Override
    public Optional<String> extractSubject(String token, String key) {
        log.debug("Extracting subject from signed token");
        return extractClaim(token, Claims::getSubject, key);
    }

    /**
     * Extract claim from token given a claim key and expected claim class.
     *
     * @param <T>        Type of claim.
     * @param token      JWT token string.
     * @param claimKey   Claim key to extract.
     * @param claimClass Expected class of claim value.
     * @return Optional containing claim if present and valid.
     */
    public <T> Optional<T> extractClaim(String token, String claimKey, Class<T> claimClass) {
        log.debug("Extracting claim: {}", claimKey);
        Claims claims = extractAllClaims(token);
        return Optional.ofNullable(claims.get(claimKey, claimClass));
    }

    @Override
    public Claims extractAllClaims(String token) {
        log.debug("Extracting all claims (unsigned) from token");
        try {
            // Extract claims without verifying signature
            return Jwts.parser()
                    .parseClaimsJwt(token.substring(0, token.lastIndexOf('.') + 1))
                    .getBody();
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Failed to parse unsigned claims: {}", ex.getMessage());
            throw new TokenInvalidException("Failed to parse JWT claims", ex);
        }
    }

    @Override
    public Claims extractAllClaims(String token, String key) {
        log.debug("Extracting all claims from signed token");
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            log.error("Jwt expired {}", ex.getMessage());
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Failed to parse signed claims: {}", ex.getMessage());
            throw new TokenInvalidException("Failed to parse JWT claims with signing key", ex);
        }
    }

    @Override
    public Optional<Date> extractExpiration(String token, String key) {
        log.debug("Extracting expiration date from signed token");
        return extractClaim(token, Claims::getExpiration, key);
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver, String key) {
        log.debug("Extracting claim with key");
        Claims claims = extractAllClaims(token, key);
        return Optional.ofNullable(claimsResolver.apply(claims));
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.debug("Extracting claim");
        Claims claims = extractAllClaims(token);
        return Optional.ofNullable(claimsResolver.apply(claims));
    }

    @Override
    public Boolean isTokenExpired(String token, String key) {
        log.debug("Checking if token is expired");
        return extractExpiration(token, key)
                .map(exp -> exp.before(Date.from(Instant.now())))
                .orElse(true); // If expiration missing or extraction failed, treat as expired
    }

    @Override
    public TokenResponseDto createToken(String subject, Map<String, Object> claims, String issuer, String audience,
                                        SignatureAlgorithm algorithm, String key, Integer lifeTimeInMs) {

        log.info("Creating new JWT token for subject: {}", subject);
        Date expiryDate = calcExpiryDate(lifeTimeInMs);

        JwtBuilder jwtBuilder = Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(expiryDate)
                .setAudience(audience)
                .signWith(algorithm, key);

        if (!CollectionUtils.isEmpty(claims)) {
            claims.forEach(jwtBuilder::claim);
        }

        String token = jwtBuilder.compact();
        log.info("JWT token created successfully");

        return new TokenResponseDto(IEnumWebToken.Types.Bearer, token, expiryDate);
    }

    /**
     * Validates the token with the given signing key and expected subject.
     * Throws TokenInvalidException if validation fails.
     *
     * @param token   JWT token string to validate.
     * @param subject Expected subject to match.
     * @param key     Signing key.
     */
    @Override
    public void validateToken(String token, String subject, String key) {
        log.info("Validating JWT token for subject: {}", subject);

        if (!StringUtils.hasText(token)) {
            log.error("Invalid JWT: token is null or empty");
            throw new TokenInvalidException("Invalid JWT token: null or empty");
        }
        try {
            // Validate signature and parse claims
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);

            // Validate subject matches expected subject
            extractSubject(token, key)
                    .filter(sub -> StringUtils.hasText(sub) && sub.equalsIgnoreCase(subject))
                    .orElseThrow(() -> new TokenInvalidException("Invalid JWT: subject does not match"));

            log.info("JWT token is valid");

        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
            throw new TokenInvalidException("Invalid JWT: signature", ex);
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT format");
            throw new TokenInvalidException("Invalid JWT: malformed", ex);
        } catch (ExpiredJwtException ex) {
            log.error("JWT token has expired");
            throw new TokenInvalidException("Invalid JWT: expired", ex);
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
            throw new TokenInvalidException("Invalid JWT: unsupported", ex);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty or illegal");
            throw new TokenInvalidException("Invalid JWT: illegal argument", ex);
        }
    }

    @Override
    public Date calcExpiryDate(Integer lifeTimeInMs) {
        log.debug("Calculating expiration date for lifetime: {}ms", lifeTimeInMs);
        return Date.from(Instant.now().plusMillis(lifeTimeInMs));
    }
}