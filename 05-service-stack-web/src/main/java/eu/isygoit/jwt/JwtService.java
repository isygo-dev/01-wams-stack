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
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String extractDomain(String token) {
        Claims claims = this.extractAllClaims(token);
        if (!CollectionUtils.isEmpty(claims) && claims.containsKey(JwtConstants.JWT_SENDER_DOMAIN)) {
            return claims.get(JwtConstants.JWT_SENDER_DOMAIN, String.class);
        }
        return null;
    }

    @Override
    public Boolean extractIsAdmin(String token) {
        Claims claims = this.extractAllClaims(token);
        if (!CollectionUtils.isEmpty(claims) && claims.containsKey(JwtConstants.JWT_IS_ADMIN)) {
            return claims.get(JwtConstants.JWT_IS_ADMIN, Boolean.class);
        }
        return null;
    }

    @Override
    public String extractApplication(String token) {
        Claims claims = this.extractAllClaims(token);
        if (!CollectionUtils.isEmpty(claims) && claims.containsKey(JwtConstants.JWT_LOG_APP)) {
            return claims.get(JwtConstants.JWT_LOG_APP, String.class);
        }
        return null;
    }

    @Override
    public String extractAccountType(String token) {
        Claims claims = this.extractAllClaims(token);
        if (!CollectionUtils.isEmpty(claims) && claims.containsKey(JwtConstants.JWT_SENDER_ACCOUNT_TYPE)) {
            return claims.get(JwtConstants.JWT_SENDER_ACCOUNT_TYPE, String.class);
        }
        return null;
    }

    @Override
    public String extractUserName(String token) {
        Claims claims = this.extractAllClaims(token);
        if (!CollectionUtils.isEmpty(claims) && claims.containsKey(JwtConstants.JWT_SENDER_USER)) {
            return claims.get(JwtConstants.JWT_SENDER_USER, String.class);
        }
        return null;
    }

    @Override
    public String extractSubject(String token, String key) {
        return extractClaim(token, Claims::getSubject, key);
    }

    @Override
    public Claims extractAllClaims(String token) {
        //This is To avoid signing check !!!!!!!
        int i = token.lastIndexOf('.');
        token = token.substring(0, i + 1);
        return Jwts.parser().parseClaimsJwt(token).getBody();
    }

    /* Signed */

    @Override
    public Date extractExpiration(String token, String key) {
        return extractClaim(token, Claims::getExpiration, key);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, String key) {
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public Claims extractAllClaims(String token, String key) {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
    }

    @Override
    public Boolean isTokenExpired(String token, String key) {
        return extractExpiration(token, key).before(new Date());
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
            final String jwtSubject = extractSubject(token, key);
            if (StringUtils.hasText(jwtSubject) && !jwtSubject.equalsIgnoreCase(subject)) {
                throw new TokenInvalidException("Invalid JWT:subject not matching");
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
