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

class JwtServiceTest {

    private JwtService jwtService;

    private final String testKey = "testkeytestkeytestkeytestkey";

    private final String subject = "testSubject";
    private final Map<String, Object> claims = Map.of(
            JwtConstants.JWT_SENDER_DOMAIN, "example.com",
            JwtConstants.JWT_IS_ADMIN, true,
            JwtConstants.JWT_LOG_APP, "TestApp",
            JwtConstants.JWT_SENDER_ACCOUNT_TYPE, "premium",
            JwtConstants.JWT_SENDER_USER, "testuser"
    );

    @BeforeEach
    void setup() {
        jwtService = new JwtService();
    }

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
        assertEquals("example.com", jwtService.extractDomain(token).orElse(null));
        assertTrue(jwtService.extractIsAdmin(token));
        assertEquals("TestApp", jwtService.extractApplication(token).orElse(null));
        assertEquals("premium", jwtService.extractAccountType(token).orElse(null));
        assertEquals("testuser", jwtService.extractUserName(token).orElse(null));
    }

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

        Optional<String> domain = jwtService.extractClaim(token, JwtConstants.JWT_SENDER_DOMAIN, String.class);
        assertTrue(domain.isPresent());
        assertEquals("example.com", domain.get());
    }

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

    @Nested
    class ValidateTokenTests {

        private String validToken;

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
            String invalidToken = "invalid.token.value";

            assertThrows(TokenInvalidException.class, () -> jwtService.extractDomain(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractSubject(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractApplication(invalidToken));
            assertThrows(TokenInvalidException.class, () -> jwtService.extractIsAdmin(invalidToken));
        }

        @Test
        void extractClaimWithNullTokenShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> jwtService.extractSubject(null));
            assertThrows(NullPointerException.class, () -> jwtService.extractDomain(null));
            assertThrows(NullPointerException.class, () -> jwtService.extractApplication(null));
            assertThrows(NullPointerException.class, () -> jwtService.extractIsAdmin(null));
        }
    }

    @Test
    void testCalcExpiryDate() {
        Date now = new Date();
        int offsetMs = 1000 * 60 * 5;

        Date expiry = jwtService.calcExpiryDate(offsetMs);

        assertTrue(expiry.after(now));
        long diff = expiry.getTime() - now.getTime();
        assertTrue(diff >= offsetMs);
    }
}