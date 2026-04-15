package eu.isygoit.jwt;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.TokenResponseDto;
import eu.isygoit.enums.IEnumWebToken;
import eu.isygoit.exception.TokenInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Complete JwtServiceTest for JJWT 0.13.0+.
 * Covers ALL original tests + full dual (secured + unsecured) extraction paths.
 * Uses a strong 512-bit key to satisfy RFC 7518.
 */
class JwtServiceTest {

    private final String testKey = "thisIsASecretKeyForHS256ThatIsLongEnough1234567890ABCDEF"; // 64 chars = 512 bits
    private final String subject = "testSubject";
    private final Map<String, Object> claims = Map.of(
            JwtConstants.JWT_SENDER_TENANT, "example.com",
            JwtConstants.JWT_IS_ADMIN, true,
            JwtConstants.JWT_LOG_APP, "TestApp",
            JwtConstants.JWT_SENDER_ACCOUNT_TYPE, "premium",
            JwtConstants.JWT_SENDER_USER, "testuser"
    );

    private JwtService jwtService;

    @BeforeEach
    void setup() {
        jwtService = new JwtService();
    }

    // ========================================================================
    // Token Creation + Basic Claims (both secured & unsecured)
    // ========================================================================

    @Test
    void testCreateTokenAndAllExtractors() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(claims)
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        String token = dto.getToken();

        assertNotNull(dto);
        assertEquals(IEnumWebToken.Types.Bearer, dto.getType());
        assertNotNull(dto.getToken());
        assertNotNull(dto.getExpiryDate());

        // Unsecured extractors (no key)
        assertEquals(subject, jwtService.extractSubject(token).orElse(null));
        assertEquals("example.com", jwtService.extractTenant(token).orElse(null));
        assertTrue(jwtService.extractIsAdmin(token));
        assertEquals("TestApp", jwtService.extractApplication(token).orElse(null));
        assertEquals("premium", jwtService.extractAccountType(token).orElse(null));
        assertEquals("testuser", jwtService.extractUserName(token).orElse(null));

        // Secured extractors (with key)
        assertEquals(subject, jwtService.extractSubject(token, testKey).orElse(null));
        assertEquals("example.com", jwtService.extractTenant(token, testKey).orElse(null));
        assertTrue(jwtService.extractIsAdmin(token, testKey));
        assertEquals("TestApp", jwtService.extractApplication(token, testKey).orElse(null));
        assertEquals("premium", jwtService.extractAccountType(token, testKey).orElse(null));
        assertEquals("testuser", jwtService.extractUserName(token, testKey).orElse(null));
    }

    // ========================================================================
    // Subject extraction (both versions)
    // ========================================================================

    @Test
    void testExtractSubjectUnsigned() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(Collections.emptyMap())
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        String token = dto.getToken();
        Optional<String> extracted = jwtService.extractSubject(token);
        assertTrue(extracted.isPresent());
        assertEquals(subject, extracted.get());
    }

    @Test
    void testExtractSubjectWithKey() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(Collections.emptyMap())
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        String token = dto.getToken();
        Optional<String> extracted = jwtService.extractSubject(token, testKey);
        assertTrue(extracted.isPresent());
        assertEquals(subject, extracted.get());
    }

    // ========================================================================
    // Generic claim extraction (both function + key and function no-key)
    // ========================================================================

    @Test
    void testExtractClaimWithFunctionAndKey() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(Collections.emptyMap())
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        String token = dto.getToken();
        Optional<Date> expiration = jwtService.extractClaim(token, Claims::getExpiration, testKey);
        assertTrue(expiration.isPresent());
    }

    @Test
    void testExtractClaimWithFunctionNoKey() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(Collections.emptyMap())
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        String token = dto.getToken();
        Optional<Date> expiration = jwtService.extractClaim(token, Claims::getExpiration);
        assertTrue(expiration.isPresent());
    }

    @Test
    void testExtractClaimWithClaimKeyAndClass() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(claims)
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        String token = dto.getToken();
        Optional<String> tenant = jwtService.extractClaim(token, JwtConstants.JWT_SENDER_TENANT, String.class);
        assertTrue(tenant.isPresent());
        assertEquals("example.com", tenant.get());
    }

    // ========================================================================
    // All claims extraction (both signed & unsigned)
    // ========================================================================

    @Test
    void testExtractAllClaimsSigned() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(Collections.emptyMap())
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        String token = dto.getToken();
        assertDoesNotThrow(() -> {
            Claims claims = jwtService.extractAllClaims(token, testKey);
            assertNotNull(claims);
            assertEquals(subject, claims.getSubject());
        });
    }

    @Test
    void testExtractAllClaimsUnsigned() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(Collections.emptyMap())
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        String token = dto.getToken();
        assertDoesNotThrow(() -> {
            Claims claims = jwtService.extractAllClaims(token);
            assertNotNull(claims);
            assertEquals(subject, claims.getSubject());
        });
    }

    @Test
    void testExtractAllClaimsUnsignedOnHs256TokenSucceeds() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(claims)
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        Claims claims = jwtService.extractAllClaims(dto.getToken());
        assertNotNull(claims);
        assertEquals(subject, claims.getSubject());
        assertEquals("example.com", claims.get(JwtConstants.JWT_SENDER_TENANT));
        assertTrue(claims.get(JwtConstants.JWT_IS_ADMIN, Boolean.class));
    }

    // ========================================================================
    // Expiration checks
    // ========================================================================

    @Test
    void testIsTokenExpiredFalse() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(Collections.emptyMap())
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        String token = dto.getToken();
        assertFalse(jwtService.isTokenExpired(token, testKey));
    }

    @Test
    void testIsTokenExpiredTrue() throws InterruptedException {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(Collections.emptyMap())
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1)
                .build());

        String token = dto.getToken();
        Thread.sleep(5);

        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenExpired(token, testKey));
    }

    // ========================================================================
    // Expiry date calculation
    // ========================================================================

    @Test
    void testCalcExpiryDate() {
        Date now = new Date();
        int offsetMs = 1000 * 60 * 5;

        Date expiry = jwtService.calcExpiryDate(offsetMs);

        assertTrue(expiry.after(now));
        long diff = expiry.getTime() - now.getTime();
        assertTrue(diff >= offsetMs);
    }

    // ========================================================================
    // Token Validation (full original nested suite)
    // ========================================================================

    @Test
    void extractMissingClaimReturnsEmptyOptional() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(Collections.emptyMap())
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        String token = dto.getToken();

        assertTrue(jwtService.extractTenant(token).isEmpty());
        assertFalse(jwtService.extractIsAdmin(token)); // default false
        assertTrue(jwtService.extractApplication(token).isEmpty());
    }

    // ========================================================================
    // Extract Claims Error Handling (covers both secured & unsecured)
    // ========================================================================

    @Test
    void extractMissingClaimSecuredReturnsEmptyOptional() {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(subject)
                .claims(Collections.emptyMap())
                .issuer("issuerTest")
                .audience("audienceTest")
                .algorithm(Jwts.SIG.HS256)
                .key(testKey)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());

        String token = dto.getToken();

        assertTrue(jwtService.extractTenant(token, testKey).isEmpty());
        assertFalse(jwtService.extractIsAdmin(token, testKey));
        assertTrue(jwtService.extractApplication(token, testKey).isEmpty());
    }

    // ========================================================================
    // Additional useful edge-case tests
    // ========================================================================

    @Nested
    class ValidateTokenTests {

        private String validToken;

        @BeforeEach
        void generateValidToken() {
            TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                    .subject(subject)
                    .claims(Collections.emptyMap())
                    .issuer("issuerTest")
                    .audience("audienceTest")
                    .algorithm(Jwts.SIG.HS256)
                    .key(testKey)
                    .lifeTimeInMs(1000 * 60 * 60)
                    .build());
            validToken = dto.getToken();
        }

        @Test
        void validateTokenSuccess() {
            assertDoesNotThrow(() -> jwtService.validateToken(validToken, subject, testKey));
        }

        @Test
        void validateTokenEmptyThrows() {
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken("", subject, testKey));
            assertTrue(ex.getMessage().contains("null or empty"));
        }

        @Test
        void validateTokenWrongSubjectThrows() {
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(validToken, "wrongSubject", testKey));
            assertTrue(ex.getMessage().contains("subject does not match"));
        }

        @Test
        void validateTokenInvalidSignatureThrows() {
            String invalidSignatureToken = validToken + "junk";
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(invalidSignatureToken, subject, testKey));
            assertTrue(ex.getMessage().toLowerCase().contains("signature"));
        }

        @Test
        void validateTokenMalformedThrows() {
            String malformedToken = "abc.def";
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(malformedToken, subject, testKey));
            assertTrue(ex.getMessage().toLowerCase().contains("malformed"));
        }

        @Test
        void validateTokenExpiredThrows() throws InterruptedException {
            TokenResponseDto shortLived = jwtService.createToken(JwtTokenRequest.builder()
                    .subject(subject)
                    .claims(Collections.emptyMap())
                    .issuer("issuerTest")
                    .audience("audienceTest")
                    .algorithm(Jwts.SIG.HS256)
                    .key(testKey)
                    .lifeTimeInMs(1)
                    .build());
            Thread.sleep(5);

            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(shortLived.getToken(), subject, testKey));
            assertTrue(ex.getMessage().toLowerCase().contains("expired"));
        }

        @Test
        void validateTokenUnsupportedThrows() {
            String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.";
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(unsupportedToken, subject, testKey));
            assertTrue(ex.getMessage().toLowerCase().contains("unsupported"));
        }

        @Test
        void validateTokenIllegalArgumentThrows() {
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(null, subject, testKey));
            assertTrue(ex.getMessage().toLowerCase().contains("null or empty"));
        }
    }

    @Nested
    class ExtractClaimsErrorHandling {

        @Test
        void extractClaimWithInvalidTokenShouldThrowTokenInvalidException() {
            String invalid = "invalid.token.value";

            // Unsecured
            assertThrows(TokenInvalidException.class, () -> jwtService.extractTenant(invalid));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractIsAdmin(invalid));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractApplication(invalid));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractAccountType(invalid));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractUserName(invalid));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractSubject(invalid));

            // Secured
            assertThrows(TokenInvalidException.class, () -> jwtService.extractTenant(invalid, testKey));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractIsAdmin(invalid, testKey));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractApplication(invalid, testKey));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractAccountType(invalid, testKey));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractUserName(invalid, testKey));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractSubject(invalid, testKey));
        }

        @Test
        void extractClaimWithNullTokenShouldThrowIllegalArgumentException() {
            // Unsecured
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractTenant(null));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractIsAdmin(null));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractApplication(null));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractAccountType(null));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractUserName(null));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractSubject(null));

            // Secured
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractTenant(null, testKey));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractIsAdmin(null, testKey));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractApplication(null, testKey));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractAccountType(null, testKey));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractUserName(null, testKey));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractSubject(null, testKey));
        }

        @Test
        void extractClaimWithEmptyTokenShouldThrowIllegalArgumentException() {
            // Unsecured
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractTenant(""));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractIsAdmin(""));

            // Secured
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractTenant("", testKey));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractIsAdmin("", testKey));
        }
    }
}