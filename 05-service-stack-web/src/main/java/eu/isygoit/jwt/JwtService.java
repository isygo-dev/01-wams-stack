package eu.isygoit.jwt;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.TokenDto;
import eu.isygoit.enums.IEnumWebToken;
import eu.isygoit.exception.TokenInvalidException;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * The type Jwt service.
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
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Optional<String> extractSubject(String token, String key) {
        return extractClaim(token, Claims::getSubject, key);
    }

    @Override
    public Optional<String> extractDomain(String token) {
        return extractClaimFromClaims(token, JwtConstants.JWT_SENDER_DOMAIN).map(Objects::toString);
    }

    @Override
    public Optional<String> extractDomain(String token, String key) {
        return extractClaimFromClaims(token, JwtConstants.JWT_SENDER_DOMAIN, key).map(Objects::toString);
    }

    @Override
    public Optional<Boolean> extractIsAdmin(String token) {
        return extractClaimFromClaims(token, JwtConstants.JWT_IS_ADMIN).map(o -> (Boolean) o);
    }

    @Override
    public Optional<Boolean> extractIsAdmin(String token, String key) {
        return extractClaimFromClaims(token, JwtConstants.JWT_IS_ADMIN, key).map(o -> (Boolean) o);
    }

    @Override
    public Optional<String> extractApplication(String token) {
        return extractClaimFromClaims(token, JwtConstants.JWT_LOG_APP).map(Objects::toString);
    }

    @Override
    public Optional<String> extractApplication(String token, String key) {
        return extractClaimFromClaims(token, JwtConstants.JWT_LOG_APP, key).map(Objects::toString);
    }

    @Override
    public Optional<String> extractAccountType(String token) {
        return extractClaimFromClaims(token, JwtConstants.JWT_SENDER_ACCOUNT_TYPE).map(Objects::toString);
    }

    @Override
    public Optional<String> extractAccountType(String token, String key) {
        return extractClaimFromClaims(token, JwtConstants.JWT_SENDER_ACCOUNT_TYPE, key).map(Objects::toString);
    }

    @Override
    public Optional<String> extractUserName(String token) {
        return extractClaimFromClaims(token, JwtConstants.JWT_SENDER_USER).map(Objects::toString);
    }

    @Override
    public Optional<String> extractUserName(String token, String key) {
        return extractClaimFromClaims(token, JwtConstants.JWT_SENDER_USER, key).map(Objects::toString);
    }

    /* Signed */

    @Override
    public Optional<Date> extractExpiration(String token, String key) {
        return extractClaim(token, Claims::getExpiration, key);
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver, String key) {
        return extractAllClaims(token, key).map(claimsResolver);
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver) {
        return extractAllClaims(token).map(claimsResolver);
    }

    @Override
    public Optional<Claims> extractAllClaims(String token) {
        return Optional.ofNullable(Jwts.parser().parseClaimsJwt(token).getBody());
    }

    @Override
    public Optional<Claims> extractAllClaims(String token, String key) {
        return Optional.ofNullable(Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody());
    }

    @Override
    public Boolean isTokenExpired(String token, String key) {
        return extractExpiration(token, key).map(exp -> exp.before(new Date())).orElse(Boolean.FALSE);
    }

    @Override
    public TokenDto createToken(String subject, Map<String, Object> claims, String issuer, String audience, SignatureAlgorithm algorithm, String key, Integer lifeTimeInMs) {
        Date expiryDate = calcExpiryDate(lifeTimeInMs);
        JwtBuilder jwtBuilder = Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .setAudience(audience)
                .signWith(algorithm, key);

        Optional.ofNullable(claims).ifPresent(c -> c.forEach(jwtBuilder::claim));

        return TokenDto.builder()
                .type(IEnumWebToken.Types.Bearer)
                .token(jwtBuilder.compact())
                .expiryDate(expiryDate)
                .build();
    }

    @Override
    public Date calcExpiryDate(Integer lifeTimeInMs) {
        return new Date(System.currentTimeMillis() + lifeTimeInMs);
    }

    public void validateToken(String token, String subject, String key) {
        if (!StringUtils.hasText(token)) {
            logAndThrow("Invalid JWT: null or empty", null);
        }

        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);

            extractSubject(token, key).ifPresentOrElse(
                    sub -> {
                        if (!sub.equalsIgnoreCase(subject)) {
                            logAndThrow("Invalid JWT: subject not matching", null);
                        }
                    },
                    () -> logAndThrow("Invalid JWT: subject missing", null)
            );
        } catch (JwtException ex) {
            handleJwtException(ex);
        }
    }

    private void logAndThrow(String message, Exception ex) {
        log.error("<Error>: {}", message, ex);
        throw new TokenInvalidException(message, ex);
    }

    /**
     * Handle jwt exception.
     *
     * @param ex the ex
     */
    public void handleJwtException(JwtException ex) {
        if (ex instanceof SignatureException) {
            logAndThrow("Invalid JWT: signature", ex);
        } else if (ex instanceof MalformedJwtException) {
            logAndThrow("Invalid JWT: malformed", ex);
        } else if (ex instanceof ExpiredJwtException) {
            logAndThrow("Invalid JWT: expired", ex);
        } else if (ex instanceof UnsupportedJwtException) {
            logAndThrow("Invalid JWT: unsupported", ex);
        } else {
            logAndThrow("Invalid JWT: unknown error", ex);
        }
    }

    private Optional<Object> extractClaimFromClaims(String token, String claimKey) {
        return extractAllClaims(token)
                .filter(claims -> claims.containsKey(claimKey))
                .map(claims -> claims.get(claimKey, Object.class));
    }

    private Optional<Object> extractClaimFromClaims(String token, String claimKey, String key) {
        return extractAllClaims(token, key)
                .filter(claims -> claims.containsKey(claimKey))
                .map(claims -> claims.get(claimKey, Object.class));
    }
}