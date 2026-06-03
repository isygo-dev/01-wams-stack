package eu.isygoit.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.TokenResponseDto;
import eu.isygoit.enums.IEnumWebToken;
import eu.isygoit.exception.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import io.jsonwebtoken.security.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting tenant from unsigned token");
        return extractClaim(token, JwtConstants.JWT_SENDER_TENANT, String.class);
    }

    @Override
    public Boolean extractIsAdmin(String token) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting isAdmin flag from unsigned token");
        return extractClaim(token, JwtConstants.JWT_IS_ADMIN, Boolean.class)
                .map(Boolean.class::cast)
                .orElse(Boolean.FALSE);
    }

    @Override
    public Optional<String> extractApplication(String token) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting application from unsigned token");
        return extractClaim(token, JwtConstants.JWT_LOG_APP, String.class);
    }

    @Override
    public Optional<String> extractAccountType(String token) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting account type from unsigned token");
        return extractClaim(token, JwtConstants.JWT_SENDER_ACCOUNT_TYPE, String.class);
    }

    @Override
    public Optional<String> extractUserName(String token) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting username from unsigned token");
        return extractClaim(token, JwtConstants.JWT_SENDER_USER, String.class);
    }

    @Override
    public Optional<String> extractSubject(String token) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting subject from unsigned token");
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Optional<String> extractIssuer(String token) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting subject from unsigned token");
        return extractClaim(token, Claims::getIssuer);
    }

    @Override
    public Optional<Set<String>> extractAudience(String token) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting subject from unsigned token");
        return extractClaim(token, Claims::getAudience);
    }

    // ========================================================================
    // SECURED convenience extractors (with key)
    // ========================================================================

    @Override
    public Optional<String> extractTenant(String token, String key) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting tenant from signed token");
        return extractClaim(token, JwtConstants.JWT_SENDER_TENANT, String.class, key);
    }

    @Override
    public Boolean extractIsAdmin(String token, String key) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting isAdmin flag from signed token");
        return extractClaim(token, JwtConstants.JWT_IS_ADMIN, Boolean.class, key)
                .map(Boolean.class::cast)
                .orElse(Boolean.FALSE);
    }

    @Override
    public Optional<String> extractAccountType(String token, String key) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting account type from signed token");
        return extractClaim(token, JwtConstants.JWT_SENDER_ACCOUNT_TYPE, String.class, key);
    }

    @Override
    public Optional<String> extractUserName(String token, String key) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting username from signed token");
        return extractClaim(token, JwtConstants.JWT_SENDER_USER, String.class, key);
    }

    @Override
    public Optional<String> extractSubject(String token, String key) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting subject from signed token");
        return extractClaim(token, Claims::getSubject, key);
    }

    @Override
    public Optional<Set<String>> extractAudience(String token, String key) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting subject from signed token");
        return extractClaim(token, Claims::getAudience, key);
    }

    @Override
    public Optional<String> extractIssuer(String token, String key) {
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting subject from signed token");
        return extractClaim(token, Claims::getIssuer, key);
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
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting all claims (unsigned) from token");
        try {
            String[] parts = token.split("\\.", 3);
            if (parts.length < 2) throw new TokenInvalidException("Invalid JWT format");

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
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

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
    public TokenResponseDto createToken(String subject, Map<String, Object> claims, String issuer, Set<String> audience,
                                        SecureDigestAlgorithm<?, ?> algorithm, String key, Integer lifeTimeInMs) {
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

        JwtBuilder jwtBuilder = Jwts.builder()
                .subject(request.subject())
                .issuer(request.issuer())
                .issuedAt(Date.from(Instant.now()))
                .expiration(expiryDate)
                .audience().add(request.audience()).and();

        // Determine algorithm type and sign accordingly
        if (request.algorithm() instanceof MacAlgorithm) {
            // HMAC algorithm (HS256, HS384, HS512)
            SecretKey secretKey = buildSecretKey(request.key()); // expects Base64
            jwtBuilder.signWith(secretKey, (SecureDigestAlgorithm<SecretKey, ?>) request.algorithm());
        } else if (request.algorithm() instanceof SignatureAlgorithm) {
            // Asymmetric algorithm (RS*, PS*, ES*, EdDSA)
            PrivateKey privateKey = loadPrivateKeyFromPem(request.key()); // expects PEM
            jwtBuilder.signWith(privateKey, (SecureDigestAlgorithm<PrivateKey, ?>) request.algorithm());
        } else {
            throw new SignaturAlgorithmNotSupportedException("Unsupported algorithm type: " + request.algorithm());
        }

        // Add claims if any
        if (!CollectionUtils.isEmpty(request.claims())) {
            request.claims().forEach(jwtBuilder::claim);
        }

        String token = jwtBuilder.compact();
        log.info("JWT token created successfully for subject: {}", request.subject());
        return new TokenResponseDto(IEnumWebToken.Types.Bearer, token, expiryDate);
    }

    private PrivateKey loadPrivateKeyFromPem(String pem) {
        try {
            // Remove headers, footers, and whitespace
            String base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(base64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

            // Try RSA first, then EC (or use Bouncy Castle for generic detection)
            try {
                KeyFactory kf = KeyFactory.getInstance("RSA");
                return kf.generatePrivate(spec);
            } catch (Exception e) {
                KeyFactory kf = KeyFactory.getInstance("EC");
                return kf.generatePrivate(spec);
            }
        } catch (Exception e) {
            throw new PrivateKeyFromPEMException("Failed to load private key from PEM", e);
        }
    }

    @Override
    public void validateToken(String token, String subject, String issuer, Set<String> audience, String secretKey, String publicKeyPem) {
        log.info("Validating JWT token for subject: {}", subject);
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }
        try {
            // 1. Manually decode the JWT header to get the algorithm (no verification)
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new TokenInvalidException("Invalid JWT format");
            }
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            // Use a simple JSON parser; if Jackson is not available, you can use a regex.
            // Here we assume Jackson ObjectMapper is available (or you can use a simple string search).
            ObjectMapper mapper = new ObjectMapper();
            JsonNode header = mapper.readTree(headerJson);
            String algName = header.get("alg").asText();

            // 2. Look up the algorithm in JJWT's registry
            SecureDigestAlgorithm<?, ?> algorithm = Jwts.SIG.get().get(algName);
            if (algorithm == null) {
                throw new SignaturAlgorithmNotSupportedException("Unsupported algorithm: " + algName);
            }

            // 3. Build the parser with the correct key
            JwtParserBuilder parserBuilder = Jwts.parser();

            if (algorithm instanceof MacAlgorithm) {
                // HMAC: use the symmetric secret key (Base64)
                SecretKey key = buildSecretKey(secretKey);
                parserBuilder.verifyWith(key);
            } else if (algorithm instanceof SignatureAlgorithm) {
                // Asymmetric: use the public key (PEM)
                if (publicKeyPem == null || publicKeyPem.isBlank()) {
                    throw new PublicKeyFromPEMException("Public key missing for asymmetric algorithm: " + algName);
                }
                PublicKey publicKey = loadPublicKeyFromPem(publicKeyPem);
                parserBuilder.verifyWith(publicKey);
            } else {
                throw new SignaturAlgorithmNotSupportedException("Unsupported algorithm type: " + algName);
            }

            // 4. Now parse and verify the token
            Claims claims = parserBuilder.build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 5. Validate subject claim
            String tokenSubject = claims.getSubject();
            if (!StringUtils.hasText(tokenSubject) || !tokenSubject.equalsIgnoreCase(subject)) {
                throw new TokenSubjectException("Invalid JWT: subject does not match");
            }

            // 6. Validate issuer claim
            String tokenIssuer = claims.getIssuer();
            if (StringUtils.hasText(tokenIssuer) && !tokenIssuer.equalsIgnoreCase(issuer)) {
                throw new TokenIssuerException("Invalid JWT: issuer does not match");
            }

            // 7. Validate audience claim
            Set<String> tokenAudience = claims.getAudience();
            if (!CollectionUtils.isEmpty(tokenAudience) && !tokenAudience.contains(audience)) {
                throw new TokenAudienceException("Invalid JWT: audience does not match");
            }

            log.info("JWT token validated successfully for subject: {}", subject);
        } catch (JsonProcessingException ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            throw new TokenInvalidException("Invalid JWT: " + ex.getMessage(), ex);
        }
    }

    private PublicKey loadPublicKeyFromPem(String pem) {
        try {
            // Remove headers and whitespace
            String base64 = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(base64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

            // Try RSA first; fallback to EC
            try {
                KeyFactory kf = KeyFactory.getInstance("RSA");
                return kf.generatePublic(spec);
            } catch (Exception e) {
                KeyFactory kf = KeyFactory.getInstance("EC");
                return kf.generatePublic(spec);
            }
        } catch (Exception e) {
            throw new PublicKeyFromPEMException("Failed to load public key from PEM", e);
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
        if (!StringUtils.hasText(token)) {
            throw new TokenInvalidException("token cannot be null or empty");
        }

        log.debug("Extracting expiration date from signed token");
        return extractClaim(token, Claims::getExpiration, key);
    }

    @Override
    public Date calcExpiryDate(Integer lifeTimeInMs) {
        return Date.from(Instant.now().plusMillis(lifeTimeInMs));
    }

    private SecretKey buildSecretKey(String base64Key) {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        return Keys.hmacShaKeyFor(decoded);
    }
}