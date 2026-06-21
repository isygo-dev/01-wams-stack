package eu.isygoit.jwt;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.TokenResponseDto;
import eu.isygoit.enums.IEnumWebToken;
import eu.isygoit.exception.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Complete JwtServiceTest for JJWT 0.13.0+.
 * Covers ALL public methods (secured + unsecured extraction, creation, validation).
 * Uses a properly Base64-encoded 512-bit key.
 */
class JwtServiceTest {

    // Base64-encoded 512-bit key (64 bytes = 512 bits) – sufficient for HS512/HS256
    private static final String TEST_KEY_BASE64 = "dGhpc0lzQVNlY3JldEtleUZvckhTMjU2VGhhdElzTG9uZ0Vub3VnaDEyMzQ1Njc4OTBBQkNEREVGIw==";
    private static final String SUBJECT = "testSubject";
    private static final String ISSUER = "testIssuer";
    private static final Set<String> AUDIENCE = Set.of("testAudience");
    private static final Map<String, Object> CLAIMS = Map.of(
            JwtConstants.JWT_SENDER_TENANT, "example.com",
            JwtConstants.JWT_IS_ADMIN, true,
            JwtConstants.JWT_LOG_APP, "TestApp",
            JwtConstants.JWT_SENDER_ACCOUNT_TYPE, "premium",
            JwtConstants.JWT_SENDER_USER, "testuser"
    );

    private JwtService jwtService;

    @BeforeEach
    void setup() {
        jwtService = new JwtService(null); // JwtProperties not needed for tests
    }

    // ========================================================================
    // Token Creation + Basic Claims (both secured & unsecured)
    // ========================================================================

    @Test
    void testCreateTokenAndAllExtractors() {
        TokenResponseDto dto = createTestToken();
        String token = dto.getToken();

        assertNotNull(dto);
        assertEquals(IEnumWebToken.Types.Bearer, dto.getType());
        assertNotNull(dto.getToken());
        assertNotNull(dto.getExpiryDate());

        // Unsecured extractors (no key)
        assertEquals(SUBJECT, jwtService.extractSubject(token).orElse(null));
        assertEquals("example.com", jwtService.extractTenant(token).orElse(null));
        assertTrue(jwtService.extractIsAdmin(token));
        assertEquals("TestApp", jwtService.extractApplication(token).orElse(null));
        assertEquals("premium", jwtService.extractAccountType(token).orElse(null));
        assertEquals("testuser", jwtService.extractUserName(token).orElse(null));
        assertEquals(ISSUER, jwtService.extractIssuer(token).orElse(null));
        assertTrue(jwtService.extractAudience(token).orElse(Set.of()).containsAll(AUDIENCE));

        // Secured extractors (with key)
        assertEquals(SUBJECT, jwtService.extractSubject(token, TEST_KEY_BASE64).orElse(null));
        assertEquals("example.com", jwtService.extractTenant(token, TEST_KEY_BASE64).orElse(null));
        assertTrue(jwtService.extractIsAdmin(token, TEST_KEY_BASE64));
        assertEquals("premium", jwtService.extractAccountType(token, TEST_KEY_BASE64).orElse(null));
        assertEquals("testuser", jwtService.extractUserName(token, TEST_KEY_BASE64).orElse(null));
        assertEquals(ISSUER, jwtService.extractIssuer(token, TEST_KEY_BASE64).orElse(null));
        assertTrue(jwtService.extractAudience(token, TEST_KEY_BASE64).orElse(Set.of()).containsAll(AUDIENCE));
    }

    // ========================================================================
    // Header extraction (unsecured)
    // ========================================================================

    @Test
    void testExtractHeader() {
        TokenResponseDto dto = createTestTokenWithHeaders();
        String token = dto.getToken();

        Optional<String> kid = jwtService.extractKmsKeyVersionId(token);
        assertTrue(kid.isPresent());
        assertEquals("test-key-version", kid.get());

        Optional<String> customHeader = jwtService.extractHeader(token, "x-custom");
        assertTrue(customHeader.isPresent());
        assertEquals("custom-value", customHeader.get());
    }

    // ========================================================================
    // Generic claim extraction (both function + key and function no-key)
    // ========================================================================

    @Test
    void testExtractClaimWithFunctionAndKey() {
        TokenResponseDto dto = createTestToken();
        String token = dto.getToken();

        Optional<Date> expiration = jwtService.extractClaim(token, Claims::getExpiration, TEST_KEY_BASE64);
        assertTrue(expiration.isPresent());
        assertTrue(expiration.get().after(new Date()));
    }

    @Test
    void testExtractClaimWithFunctionNoKey() {
        TokenResponseDto dto = createTestToken();
        String token = dto.getToken();

        Optional<Date> expiration = jwtService.extractClaim(token, Claims::getExpiration);
        assertTrue(expiration.isPresent());
        assertTrue(expiration.get().after(new Date()));
    }

    @Test
    void testExtractClaimWithClaimKeyAndClass() {
        TokenResponseDto dto = createTestTokenWithClaims();
        String token = dto.getToken();

        Optional<String> tenant = jwtService.extractClaim(token, JwtConstants.JWT_SENDER_TENANT, String.class);
        assertTrue(tenant.isPresent());
        assertEquals("example.com", tenant.get());

        // Secured version
        Optional<String> tenantSecured = jwtService.extractClaim(token, JwtConstants.JWT_SENDER_TENANT, String.class, TEST_KEY_BASE64);
        assertTrue(tenantSecured.isPresent());
        assertEquals("example.com", tenantSecured.get());
    }

    // ========================================================================
    // All claims extraction (both signed & unsigned)
    // ========================================================================

    @Test
    void testExtractAllClaimsSigned() {
        TokenResponseDto dto = createTestToken();
        String token = dto.getToken();

        Claims claims = jwtService.extractAllClaims(token, TEST_KEY_BASE64);
        assertNotNull(claims);
        assertEquals(SUBJECT, claims.getSubject());
        assertEquals(ISSUER, claims.getIssuer());
        assertTrue(claims.getAudience().containsAll(AUDIENCE));
    }

    @Test
    void testExtractAllClaimsUnsigned() {
        TokenResponseDto dto = createTestToken();
        String token = dto.getToken();

        Claims claims = jwtService.extractAllClaims(token);
        assertNotNull(claims);
        assertEquals(SUBJECT, claims.getSubject());
        assertEquals(ISSUER, claims.getIssuer());
        assertTrue(claims.getAudience().containsAll(AUDIENCE));
    }

    @Test
    void testExtractAllClaimsUnsignedOnHs256TokenSucceeds() {
        TokenResponseDto dto = createTestTokenWithClaims();
        String token = dto.getToken();

        Claims claims = jwtService.extractAllClaims(token);
        assertNotNull(claims);
        assertEquals(SUBJECT, claims.getSubject());
        assertEquals("example.com", claims.get(JwtConstants.JWT_SENDER_TENANT));
        assertTrue(claims.get(JwtConstants.JWT_IS_ADMIN, Boolean.class));
    }

    // ========================================================================
    // Expiration checks
    // ========================================================================

    @Test
    void testIsTokenExpiredFalse() {
        TokenResponseDto dto = createTestToken();
        assertFalse(jwtService.isTokenExpired(dto.getToken(), TEST_KEY_BASE64));
    }

    @Test
    void testIsTokenExpiredTrue() throws InterruptedException {
        TokenResponseDto dto = jwtService.createToken(JwtTokenRequest.builder()
                .subject(SUBJECT)
                .claims(Collections.emptyMap())
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .algorithm(Jwts.SIG.HS256)
                .key(TEST_KEY_BASE64)
                .lifeTimeInMs(1)
                .build());

        Thread.sleep(5);

        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenExpired(dto.getToken(), TEST_KEY_BASE64));
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
        TokenResponseDto dto = createTestToken(); // no custom claims
        String token = dto.getToken();

        assertTrue(jwtService.extractTenant(token).isEmpty());
        assertFalse(jwtService.extractIsAdmin(token)); // default false
        assertTrue(jwtService.extractApplication(token).isEmpty());
    }

    @Test
    void extractMissingClaimSecuredReturnsEmptyOptional() {
        TokenResponseDto dto = createTestToken();
        String token = dto.getToken();

        assertTrue(jwtService.extractTenant(token, TEST_KEY_BASE64).isEmpty());
        assertFalse(jwtService.extractIsAdmin(token, TEST_KEY_BASE64));
    }

    // ========================================================================
    // ValidateToken tests (comprehensive)
    // ========================================================================

    @Nested
    class ValidateTokenTests {

        private String validToken;

        @BeforeEach
        void generateValidToken() {
            TokenResponseDto dto = createTestToken();
            validToken = dto.getToken();
        }

        @Test
        void validateTokenSuccess() {
            assertDoesNotThrow(() -> jwtService.validateToken(
                    validToken, SUBJECT, ISSUER, AUDIENCE, TEST_KEY_BASE64, null));
        }

        @Test
        void validateTokenWithWildcardAudience() {
            Set<String> wildcardAudience = Set.of("*");
            assertDoesNotThrow(() -> jwtService.validateToken(
                    validToken, SUBJECT, ISSUER, wildcardAudience, TEST_KEY_BASE64, null));
        }

        @Test
        void validateTokenEmptyThrows() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> jwtService.validateToken("", SUBJECT, ISSUER, AUDIENCE, TEST_KEY_BASE64, null));
            assertTrue(ex.getMessage().contains("null or empty"));
        }

        @Test
        void validateTokenWrongSubjectThrows() {
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(validToken, "wrongSubject", ISSUER, AUDIENCE, TEST_KEY_BASE64, null));
            assertTrue(ex.getMessage().contains("subject does not match"));
        }

        @Test
        void validateTokenWrongIssuerThrows() {
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(validToken, SUBJECT, "wrongIssuer", AUDIENCE, TEST_KEY_BASE64, null));
            assertTrue(ex.getMessage().contains("issuer does not match"));
        }

        @Test
        void validateTokenWrongAudienceThrows() {
            Set<String> wrongAudience = Set.of("other");
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(validToken, SUBJECT, ISSUER, wrongAudience, TEST_KEY_BASE64, null));
            assertTrue(ex.getMessage().contains("audience does not match"));
        }

        @Test
        void validateTokenInvalidSignatureThrows() {
            String invalidSignatureToken = validToken + "junk";
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(invalidSignatureToken, SUBJECT, ISSUER, AUDIENCE, TEST_KEY_BASE64, null));
            assertTrue(ex.getMessage().toLowerCase().contains("signature"));
        }

        @Test
        void validateTokenMalformedThrows() {
            String malformedToken = "abc.def";
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(malformedToken, SUBJECT, ISSUER, AUDIENCE, TEST_KEY_BASE64, null));
            assertTrue(ex.getMessage().toLowerCase().contains("malformed"));
        }

        @Test
        void validateTokenExpiredThrows() throws InterruptedException {
            TokenResponseDto shortLived = jwtService.createToken(JwtTokenRequest.builder()
                    .subject(SUBJECT)
                    .claims(Collections.emptyMap())
                    .issuer(ISSUER)
                    .audience(AUDIENCE)
                    .algorithm(Jwts.SIG.HS256)
                    .key(TEST_KEY_BASE64)
                    .lifeTimeInMs(1)
                    .build());
            Thread.sleep(5);

            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(shortLived.getToken(), SUBJECT, ISSUER, AUDIENCE, TEST_KEY_BASE64, null));
            assertTrue(ex.getMessage().toLowerCase().contains("expired"));
        }

        @Test
        void validateTokenUnsupportedAlgorithmThrows() {
            // Create a token with "none" algorithm – should be rejected
            String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.";
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(unsupportedToken, SUBJECT, ISSUER, AUDIENCE, TEST_KEY_BASE64, null));
            assertTrue(ex.getMessage().toLowerCase().contains("unsupported"));
        }

        @Test
        void validateTokenWithMissingAudienceThrows() {
            // Create token without audience
            TokenResponseDto noAudience = jwtService.createToken(JwtTokenRequest.builder()
                    .subject(SUBJECT)
                    .claims(Collections.emptyMap())
                    .issuer(ISSUER)
                    .audience(null)
                    .algorithm(Jwts.SIG.HS256)
                    .key(TEST_KEY_BASE64)
                    .lifeTimeInMs(1000 * 60)
                    .build());

            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(noAudience.getToken(), SUBJECT, ISSUER, AUDIENCE, TEST_KEY_BASE64, null));
            assertTrue(ex.getMessage().toLowerCase().contains("missing audience"));
        }

        @Test
        void validateTokenIllegalArgumentThrows() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> jwtService.validateToken(null, SUBJECT, ISSUER, AUDIENCE, TEST_KEY_BASE64, null));
            assertTrue(ex.getMessage().toLowerCase().contains("null or empty"));
        }
    }

    // ========================================================================
    // Extract Claims Error Handling (covers both secured & unsecured)
    // ========================================================================

    @Nested
    class ExtractClaimsErrorHandling {

        private final String invalidToken = "invalid.token.value";

        @Test
        void extractClaimWithInvalidTokenShouldThrowTokenInvalidException() {
            // Unsecured
            assertThrows(TokenInvalidException.class, () -> jwtService.extractTenant(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractIsAdmin(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractApplication(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractAccountType(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractUserName(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractSubject(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractIssuer(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractAudience(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractHeader(invalidToken, "any"));

            // Secured
            assertThrows(TokenInvalidException.class, () -> jwtService.extractTenant(invalidToken, TEST_KEY_BASE64));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractIsAdmin(invalidToken, TEST_KEY_BASE64));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractAccountType(invalidToken, TEST_KEY_BASE64));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractUserName(invalidToken, TEST_KEY_BASE64));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractSubject(invalidToken, TEST_KEY_BASE64));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractIssuer(invalidToken, TEST_KEY_BASE64));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractAudience(invalidToken, TEST_KEY_BASE64));
        }

        @Test
        void extractClaimWithNullTokenShouldThrowIllegalArgumentException() {
            // Unsecured
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractTenant(null));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractIsAdmin(null));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractAccountType(null));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractUserName(null));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractSubject(null));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractIssuer(null));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractAudience(null));

            // Secured
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractTenant(null, TEST_KEY_BASE64));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractIsAdmin(null, TEST_KEY_BASE64));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractAccountType(null, TEST_KEY_BASE64));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractUserName(null, TEST_KEY_BASE64));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractSubject(null, TEST_KEY_BASE64));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractIssuer(null, TEST_KEY_BASE64));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractAudience(null, TEST_KEY_BASE64));
        }

        @Test
        void extractClaimWithEmptyTokenShouldThrowIllegalArgumentException() {
            // Unsecured
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractTenant(""));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractIsAdmin(""));

            // Secured
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractTenant("", TEST_KEY_BASE64));
            assertThrows(IllegalArgumentException.class, () -> jwtService.extractIsAdmin("", TEST_KEY_BASE64));
        }
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private TokenResponseDto createTestToken() {
        return jwtService.createToken(JwtTokenRequest.builder()
                .subject(SUBJECT)
                .claims(Collections.emptyMap())
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .algorithm(Jwts.SIG.HS256)
                .key(TEST_KEY_BASE64)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());
    }

    private TokenResponseDto createTestTokenWithClaims() {
        return jwtService.createToken(JwtTokenRequest.builder()
                .subject(SUBJECT)
                .claims(CLAIMS)
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .algorithm(Jwts.SIG.HS256)
                .key(TEST_KEY_BASE64)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());
    }

    private TokenResponseDto createTestTokenWithHeaders() {
        Map<String, Object> headers = Map.of(
                JwtConstants.KID_VERSION, "test-key-version",
                "x-custom", "custom-value"
        );
        return jwtService.createToken(JwtTokenRequest.builder()
                .subject(SUBJECT)
                .claims(Collections.emptyMap())
                .headers(headers)
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .algorithm(Jwts.SIG.HS256)
                .key(TEST_KEY_BASE64)
                .lifeTimeInMs(1000 * 60 * 60)
                .build());
    }
}