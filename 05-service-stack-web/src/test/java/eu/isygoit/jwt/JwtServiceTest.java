package eu.isygoit.jwt;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.TokenDto;
import eu.isygoit.enums.IEnumWebToken;
import eu.isygoit.exception.TokenInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Jwt service test.
 */
class JwtServiceTest {

    private JwtService jwtService;

    private final String testKey = "testkeytestkeytestkeytestkey";

    private final String subject = "testSubject";
    private final Map<String, Object> claims = Map.of(
            JwtConstants.JWT_SENDER_TENANT, "example.com",
            JwtConstants.JWT_IS_ADMIN, true,
            JwtConstants.JWT_LOG_APP, "TestApp",
            JwtConstants.JWT_SENDER_ACCOUNT_TYPE, "premium",
            JwtConstants.JWT_SENDER_USER, "testuser"
    );

    /**
     * Sets .
     */
    @BeforeEach
    void setup() {
        jwtService = new JwtService();
    }

    /**
     * Test create token and basic claims.
     */
    @Test
    void testCreateTokenAndBasicClaims() {
        TokenDto tokenDto = jwtService.createToken(
                subject,
                claims,
                "issuerTest",
                "audienceTest",
                SignatureAlgorithm.HS256,
                testKey,
                1000 * 60 * 60 // 1 hour
        );

        assertNotNull(tokenDto);
        assertEquals(IEnumWebToken.Types.Bearer, tokenDto.getType());
        assertNotNull(tokenDto.getToken());
        assertNotNull(tokenDto.getExpiryDate());

        String token = tokenDto.getToken();

        // Basic claim extractions
        assertEquals(subject, jwtService.extractSubject(token, testKey).orElse(null));
        assertEquals("example.com", jwtService.extractTenant(token).orElse(null));
        assertTrue(jwtService.extractIsAdmin(token));
        assertEquals("TestApp", jwtService.extractApplication(token).orElse(null));
        assertEquals("premium", jwtService.extractAccountType(token).orElse(null));
        assertEquals("testuser", jwtService.extractUserName(token).orElse(null));
    }

    /**
     * Test extract subject unsigned.
     */
    @Test
    void testExtractSubjectUnsigned() {
        TokenDto tokenDto = jwtService.createToken(
                subject,
                Collections.emptyMap(),
                "issuerTest",
                "audienceTest",
                SignatureAlgorithm.HS256,
                testKey,
                1000 * 60 * 60 // 1 hour
        );

        String token = tokenDto.getToken();

        Optional<String> extracted = jwtService.extractSubject(token);
        assertTrue(extracted.isPresent());
        assertEquals(subject, extracted.get());
    }

    /**
     * Test extract subject with key.
     */
    @Test
    void testExtractSubjectWithKey() {
        TokenDto tokenDto = jwtService.createToken(
                subject,
                Collections.emptyMap(),
                "issuerTest",
                "audienceTest",
                SignatureAlgorithm.HS256,
                testKey,
                1000 * 60 * 60
        );

        String token = tokenDto.getToken();

        Optional<String> extracted = jwtService.extractSubject(token, testKey);
        assertTrue(extracted.isPresent());
        assertEquals(subject, extracted.get());
    }

    /**
     * Test extract claim with function and key.
     */
    @Test
    void testExtractClaimWithFunctionAndKey() {
        TokenDto tokenDto = jwtService.createToken(
                subject,
                Collections.emptyMap(),
                "issuerTest",
                "audienceTest",
                SignatureAlgorithm.HS256,
                testKey,
                1000 * 60 * 60
        );

        String token = tokenDto.getToken();

        Optional<Date> expiration = jwtService.extractClaim(token, Claims::getExpiration, testKey);
        assertTrue(expiration.isPresent());
    }

    /**
     * Test extract claim with function no key.
     */
    @Test
    void testExtractClaimWithFunctionNoKey() {
        TokenDto tokenDto = jwtService.createToken(
                subject,
                Collections.emptyMap(),
                "issuerTest",
                "audienceTest",
                SignatureAlgorithm.HS256,
                testKey,
                1000 * 60 * 60
        );

        String token = tokenDto.getToken();

        Optional<Date> expiration = jwtService.extractClaim(token, Claims::getExpiration);
        assertTrue(expiration.isPresent());
    }

    /**
     * Test extract claim with claim key and class.
     */
    @Test
    void testExtractClaimWithClaimKeyAndClass() {
        TokenDto tokenDto = jwtService.createToken(
                subject,
                claims,
                "issuerTest",
                "audienceTest",
                SignatureAlgorithm.HS256,
                testKey,
                1000 * 60 * 60
        );

        String token = tokenDto.getToken();

        Optional<String> tenant = jwtService.extractClaim(token, JwtConstants.JWT_SENDER_TENANT, String.class);
        assertTrue(tenant.isPresent());
        assertEquals("example.com", tenant.get());
    }

    /**
     * Test extract all claims signed.
     */
    @Test
    void testExtractAllClaimsSigned() {
        TokenDto tokenDto = jwtService.createToken(
                subject,
                Collections.emptyMap(),
                "issuerTest",
                "audienceTest",
                SignatureAlgorithm.HS256,
                testKey,
                1000 * 60 * 60
        );
        String token = tokenDto.getToken();

        assertDoesNotThrow(() -> {
            var claims = jwtService.extractAllClaims(token, testKey);
            assertNotNull(claims);
            assertEquals(subject, claims.getSubject());
        });
    }

    /**
     * Test extract all claims unsigned.
     */
    @Test
    void testExtractAllClaimsUnsigned() {
        TokenDto tokenDto = jwtService.createToken(
                subject,
                Collections.emptyMap(),
                "issuerTest",
                "audienceTest",
                SignatureAlgorithm.HS256,
                testKey,
                1000 * 60 * 60
        );
        String token = tokenDto.getToken();

        assertDoesNotThrow(() -> {
            var claims = jwtService.extractAllClaims(token);
            assertNotNull(claims);
            assertEquals(subject, claims.getSubject());
        });
    }

    /**
     * Test is token expired false.
     */
    @Test
    void testIsTokenExpiredFalse() {
        TokenDto tokenDto = jwtService.createToken(
                subject,
                Collections.emptyMap(),
                "issuerTest",
                "audienceTest",
                SignatureAlgorithm.HS256,
                testKey,
                1000 * 60 * 60
        );
        String token = tokenDto.getToken();

        assertFalse(jwtService.isTokenExpired(token, testKey));
    }

    /**
     * Test is token expired true.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    void testIsTokenExpiredTrue() throws InterruptedException {
        TokenDto tokenDto = jwtService.createToken(
                subject,
                Collections.emptyMap(),
                "issuerTest",
                "audienceTest",
                SignatureAlgorithm.HS256,
                testKey,
                1
        );
        String token = tokenDto.getToken();

        Thread.sleep(5);

        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenExpired(token, testKey));
    }

    /**
     * Test calc expiry date.
     */
    @Test
    void testCalcExpiryDate() {
        Date now = new Date();
        int offsetMs = 1000 * 60 * 5;

        Date expiry = jwtService.calcExpiryDate(offsetMs);

        assertTrue(expiry.after(now));
        long diff = expiry.getTime() - now.getTime();
        assertTrue(diff >= offsetMs);
    }

    /**
     * The type Validate token tests.
     */
    @Nested
    class ValidateTokenTests {

        private String validToken;

        /**
         * Generate valid token.
         */
        @BeforeEach
        void generateValidToken() {
            TokenDto tokenDto = jwtService.createToken(
                    subject,
                    Collections.emptyMap(),
                    "issuerTest",
                    "audienceTest",
                    SignatureAlgorithm.HS256,
                    testKey,
                    1000 * 60 * 60
            );
            validToken = tokenDto.getToken();
        }

        /**
         * Validate token success.
         */
        @Test
        void validateTokenSuccess() {
            assertDoesNotThrow(() -> jwtService.validateToken(validToken, subject, testKey));
        }

        /**
         * Validate token empty throws.
         */
        @Test
        void validateTokenEmptyThrows() {
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken("", subject, testKey));
            assertTrue(ex.getMessage().contains("null or empty"));
        }

        /**
         * Validate token wrong subject throws.
         */
        @Test
        void validateTokenWrongSubjectThrows() {
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(validToken, "wrongSubject", testKey));
            assertTrue(ex.getMessage().contains("subject does not match"));
        }

        /**
         * Validate token invalid signature throws.
         */
        @Test
        void validateTokenInvalidSignatureThrows() {
            String invalidSignatureToken = validToken + "junk";

            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(invalidSignatureToken, subject, testKey));
            assertTrue(ex.getMessage().toLowerCase().contains("signature"));
        }

        /**
         * Validate token malformed throws.
         */
        @Test
        void validateTokenMalformedThrows() {
            String malformedToken = "abc.def";

            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(malformedToken, subject, testKey));
            assertTrue(ex.getMessage().toLowerCase().contains("malformed"));
        }

        /**
         * Validate token expired throws.
         *
         * @throws InterruptedException the interrupted exception
         */
        @Test
        void validateTokenExpiredThrows() throws InterruptedException {
            TokenDto shortLivedTokenDto = jwtService.createToken(
                    subject,
                    Collections.emptyMap(),
                    "issuerTest",
                    "audienceTest",
                    SignatureAlgorithm.HS256,
                    testKey,
                    1
            );

            Thread.sleep(5);

            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(shortLivedTokenDto.getToken(), subject, testKey));
            assertTrue(ex.getMessage().toLowerCase().contains("expired"));
        }

        /**
         * Validate token unsupported throws.
         */
        @Test
        void validateTokenUnsupportedThrows() {
            String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.";

            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(unsupportedToken, subject, testKey));
            assertTrue(ex.getMessage().toLowerCase().contains("unsupported"));
        }

        /**
         * Validate token illegal argument throws.
         */
        @Test
        void validateTokenIllegalArgumentThrows() {
            TokenInvalidException ex = assertThrows(TokenInvalidException.class,
                    () -> jwtService.validateToken(null, subject, testKey));
            assertTrue(ex.getMessage().toLowerCase().contains("null or empty"));
        }
    }

    /**
     * The type Extract claims error handling.
     */
    @Nested
    class ExtractClaimsErrorHandling {

        /**
         * Extract claim with invalid token should throw token invalid exception.
         */
        @Test
        void extractClaimWithInvalidTokenShouldThrowTokenInvalidException() {
            String invalidToken = "invalid.token.value";

            assertThrows(TokenInvalidException.class, () -> jwtService.extractTenant(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractSubject(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractApplication(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractIsAdmin(invalidToken));
        }

        /**
         * Extract claim with null token should throw null pointer exception.
         */
        @Test
        void extractClaimWithNullTokenShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> jwtService.extractSubject(null));
            assertThrows(NullPointerException.class, () -> jwtService.extractTenant(null));
            assertThrows(NullPointerException.class, () -> jwtService.extractApplication(null));
            assertThrows(NullPointerException.class, () -> jwtService.extractIsAdmin(null));
        }
    }
}