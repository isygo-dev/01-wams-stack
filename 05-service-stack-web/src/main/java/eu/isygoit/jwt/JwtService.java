package eu.isygoit.jwt;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.TokenDto;
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
 * Service de gestion des JWT.
 */
@Slf4j
@Service
@Transactional
public class JwtService implements IJwtService {

    public static final String AUTHORIZATION = "Authorization";

    @Override
    public Optional<String> extractSubject(String token) {
        log.debug("Extracting subject from token");
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Optional<String> extractDomain(String token) {
        log.debug("Extracting domain from token");
        return extractClaim(token, JwtConstants.JWT_SENDER_DOMAIN, String.class);
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

    private <T> Optional<T> extractClaim(String token, String claimKey, Class<T> claimClass) {
        log.debug("Extracting claim: {}", claimKey);
        return Optional.ofNullable(extractAllClaims(token).get(claimKey, claimClass));
    }

    @Override
    public Claims extractAllClaims(String token) {
        log.debug("Extracting all claims (unsigned) from token");
        //This is To avoid signing check !!!!!!!
        return Jwts.parser()
                .parseClaimsJwt(token.substring(0, token.lastIndexOf('.') + 1))
                .getBody();
    }

    @Override
    public Claims extractAllClaims(String token, String key) {
        log.debug("Extracting all claims from signed token");
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public Optional<Date> extractExpiration(String token, String key) {
        log.debug("Extracting expiration date from signed token");
        return extractClaim(token, Claims::getExpiration, key);
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver, String key) {
        log.debug("Extracting claim with key");
        return Optional.ofNullable(claimsResolver.apply(extractAllClaims(token, key)));
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.debug("Extracting claim");
        return Optional.ofNullable(claimsResolver.apply(extractAllClaims(token)));
    }

    @Override
    public Boolean isTokenExpired(String token, String key) {
        log.debug("Checking if token is expired");
        return extractExpiration(token, key)
                .map(exp -> exp.before(Date.from(Instant.now())))
                .orElse(true);
    }

    @Override
    public TokenDto createToken(String subject, Map<String, Object> claims, String issuer, String audience,
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

        return new TokenDto(IEnumWebToken.Types.Bearer, token, expiryDate);
    }

    @Override
    public void validateToken(String token, String subject, String key) {
        log.info("Validating JWT token for subject: {}", subject);

        if (!StringUtils.hasText(token)) {
            log.error("Invalid JWT: token is null or empty");
            throw new TokenInvalidException("Invalid JWT token: null or empty");
        }
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
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
            log.error("JWT claims string is empty");
            throw new TokenInvalidException("Invalid JWT: illegal argument", ex);
        }
    }

    @Override
    public Date calcExpiryDate(Integer lifeTimeInMs) {
        log.debug("Calculating expiration date for lifetime: {}ms", lifeTimeInMs);
        return Date.from(Instant.now().plusMillis(lifeTimeInMs));
    }
}