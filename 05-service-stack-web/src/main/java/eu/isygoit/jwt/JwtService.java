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

import java.util.Date;
import java.util.Map;
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

    /* Not Signed */

    @Override
    public Optional<String> extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Optional<String> extractDomain(String token) {
        Optional<Claims> optional = this.extractAllClaims(token);
        if (optional.isPresent()) {
            if (!CollectionUtils.isEmpty(optional.get()) && optional.get().containsKey(JwtConstants.JWT_SENDER_DOMAIN)) {
                return optional.get().get(JwtConstants.JWT_SENDER_DOMAIN, String.class).describeConstable();
            }
        }
        return Optional.empty();
    }

    @Override
    public Boolean extractIsAdmin(String token) {
        Optional<Claims> optional = this.extractAllClaims(token);
        if (optional.isPresent()) {
            if (!CollectionUtils.isEmpty(optional.get()) && optional.get().containsKey(JwtConstants.JWT_IS_ADMIN)) {
                return optional.get().get(JwtConstants.JWT_IS_ADMIN, Boolean.class);
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public Optional<String> extractApplication(String token) {
        Optional<Claims> optional = this.extractAllClaims(token);
        if (optional.isPresent()) {
            if (!CollectionUtils.isEmpty(optional.get()) && optional.get().containsKey(JwtConstants.JWT_LOG_APP)) {
                return optional.get().get(JwtConstants.JWT_LOG_APP, String.class).describeConstable();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> extractAccountType(String token) {
        Optional<Claims> optional = this.extractAllClaims(token);
        if (optional.isPresent()) {
            if (!CollectionUtils.isEmpty(optional.get()) && optional.get().containsKey(JwtConstants.JWT_SENDER_ACCOUNT_TYPE)) {
                return optional.get().get(JwtConstants.JWT_SENDER_ACCOUNT_TYPE, String.class).describeConstable();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> extractUserName(String token) {
        Optional<Claims> optional = this.extractAllClaims(token);
        if (optional.isPresent()) {
            if (!CollectionUtils.isEmpty(optional.get()) && optional.get().containsKey(JwtConstants.JWT_SENDER_USER)) {
                return optional.get().get(JwtConstants.JWT_SENDER_USER, String.class).describeConstable();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> extractSubject(String token, String key) {
        return extractClaim(token, Claims::getSubject, key);
    }

    @Override
    public Optional<Claims> extractAllClaims(String token) {
        //This is To avoid signing check !!!!!!!
        int i = token.lastIndexOf('.');
        token = token.substring(0, i + 1);
        return Optional.ofNullable(Jwts.parser().parseClaimsJwt(token).getBody());
    }

    /* Signed */

    @Override
    public Optional<Date> extractExpiration(String token, String key) {
        return extractClaim(token, Claims::getExpiration, key);
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver, String key) {
        final Optional<Claims> optional = extractAllClaims(token, key);
        if (optional.isPresent()) {
            return Optional.ofNullable(claimsResolver.apply(optional.get()));
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Optional<Claims> optional = extractAllClaims(token);
        if (optional.isPresent()) {
            return Optional.ofNullable(claimsResolver.apply(optional.get()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Claims> extractAllClaims(String token, String key) {
        return Optional.ofNullable(Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody());
    }

    @Override
    public Boolean isTokenExpired(String token, String key) {
        Optional<Date> optional = extractExpiration(token, key);
        if (optional.isPresent()) {
            return optional.get().before(new Date());
        }
        return Boolean.FALSE;
    }

    @Override
    public TokenDto createToken(String subject, Map<String, Object> claims, String issuer, String audience
            , SignatureAlgorithm algorithm, String key, Integer lifeTimeInMs) {

        Date expiryDate = calcExpiryDate(lifeTimeInMs);
        JwtBuilder jwtBuilder = Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiryDate)
                .setAudience(audience)
                .signWith(algorithm, key);

        if (!CollectionUtils.isEmpty(claims)) {
            claims.forEach((k, v) -> {
                jwtBuilder.claim(k, v);
            });
        }

        return TokenDto.builder()
                .type(IEnumWebToken.Types.Bearer)
                .token(jwtBuilder.compact())
                .expiryDate(expiryDate)
                .build();
    }

    @Override
    public void validateToken(String token, String subject, String key) {
        if (!StringUtils.hasText(token)) {
            log.error("<Error>: Invalid JWT: null or empty");
            throw new TokenInvalidException("Invalid JWT token: null or empty");
        }
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            final Optional<String> optional = extractSubject(token, key);
            if (!optional.isPresent()) {
                if (StringUtils.hasText(optional.get()) && !optional.get().equalsIgnoreCase(subject)) {
                    throw new TokenInvalidException("Invalid JWT:subject not matching");
                }
            }
        } catch (SignatureException ex) {
            log.error("<Error>: Invalid JWT signature");
            throw new TokenInvalidException("Invalid JWT:signature", ex);
        } catch (MalformedJwtException ex) {
            log.error("<Error>: Invalid JWT token");
            throw new TokenInvalidException("Invalid JWT:malformed", ex);
        } catch (ExpiredJwtException ex) {
            log.error("<Error>: Expired JWT token");
            throw new TokenInvalidException("Invalid JWT:Expired", ex);
        } catch (UnsupportedJwtException ex) {
            log.error("<Error>: Unsupported JWT token");
            throw new TokenInvalidException("Invalid JWT:unsupported", ex);
        } catch (IllegalArgumentException ex) {
            log.error("<Error>: JWT claims string is empty");
            throw new TokenInvalidException("Invalid JWT:illegal", ex);
        }
    }

    @Override
    public Date calcExpiryDate(Integer lifeTimeInMs) {
        return new Date(new Date().getTime() + lifeTimeInMs);
    }
}
