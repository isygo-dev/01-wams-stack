package eu.isygoit.jwt;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.TokenDto;
import eu.isygoit.exception.TokenInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Jwt service test.
 */
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private JwtBuilder jwtBuilder;

    @Mock
    private Claims claims;

    private String key = "secretKey";
    private String issuer = "testIssuer";
    private String audience = "testAudience";
    private String subject = "testSubject";
    private Map<String, Object> claimsMap = Map.of(JwtConstants.JWT_SENDER_DOMAIN, "test_domain",
            JwtConstants.JWT_IS_ADMIN, Boolean.TRUE,
            JwtConstants.JWT_LOG_APP, "test_app",
            JwtConstants.JWT_SENDER_ACCOUNT_TYPE, "admin",
            JwtConstants.JWT_SENDER_USER, "test_user");
    private SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;
    private int lifeTimeInMs = 100000;
    private String token = "generated-token";

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        token = jwtService.createToken(subject, claimsMap, issuer, audience, algorithm, key, lifeTimeInMs).getToken();
    }

    /**
     * Test extract subject should return subject.
     */
    @Test
    void testExtractSubject_ShouldReturnSubject() {
        // Create a token using the service
        TokenDto tokenDto = jwtService.createToken(subject, claimsMap, issuer, audience, algorithm, key, lifeTimeInMs);

        // Extract subject from the generated token
        String token = tokenDto.getToken();
        Optional<String> extractedSubject = jwtService.extractSubject(token, key);

        assertTrue(extractedSubject.isPresent());
        assertEquals(subject, extractedSubject.get());
    }

    /**
     * Test validate token should throw exception when token is empty.
     */
    @Test
    void testValidateToken_ShouldThrowException_WhenTokenIsEmpty() {
        String token = "";

        TokenInvalidException exception = assertThrows(TokenInvalidException.class, () -> {
            jwtService.validateToken(token, subject, key);
        });

        assertEquals("Invalid JWT: null or empty", exception.getMessage());
    }

    /**
     * Test validate token should throw exception when subject does not match.
     */
    @Test
    void testValidateToken_ShouldThrowException_WhenSubjectDoesNotMatch() {
        // Create a token using the service
        TokenDto tokenDto = jwtService.createToken(subject, claimsMap, issuer, audience, algorithm, key, lifeTimeInMs);
        String token = tokenDto.getToken();

        String incorrectSubject = "incorrectSubject";

        TokenInvalidException exception = assertThrows(TokenInvalidException.class, () -> {
            jwtService.validateToken(token, incorrectSubject, key);
        });

        assertEquals("Invalid JWT: subject not matching", exception.getMessage());
    }

    /**
     * Test handle jwt exception should throw exception when malformed jwt.
     */
    @Test
    void testHandleJwtException_ShouldThrowException_WhenMalformedJwt() {
        String token = "jdhflkqjshdlkqsdf";

        TokenInvalidException exception = assertThrows(TokenInvalidException.class, () -> {
            jwtService.validateToken(token, subject, key);
        });

        assertEquals("Invalid JWT: malformed", exception.getMessage());
    }

    /**
     * Test validate token should throw exception when token is expired.
     */
    @Test
    void testValidateToken_ShouldThrowException_WhenTokenIsExpired() {
        // Create a token with a very short expiration time (e.g., 1 ms)
        int shortLifeTimeInMs = 1; // 1 millisecond to immediately expire
        TokenDto tokenDto = jwtService.createToken(subject, claimsMap, issuer, audience, algorithm, key, shortLifeTimeInMs);
        String token = tokenDto.getToken();

        // Simulate a delay to ensure the token expires
        try {
            Thread.sleep(2); // Sleep for 2ms to let the token expire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        TokenInvalidException exception = assertThrows(TokenInvalidException.class, () -> {
            jwtService.validateToken(token, subject, key);
        });

        assertEquals("Invalid JWT: expired", exception.getMessage());
    }

    /**
     * Test extract domain should return domain.
     */
    @Test
    void testExtractDomain_ShouldReturnDomain() {
        // Extract domain from the generated token
        Optional<String> extractedDomain = jwtService.extractDomain(token, key);

        assertTrue(extractedDomain.isPresent());
        assertEquals(claimsMap.get(JwtConstants.JWT_SENDER_DOMAIN), extractedDomain.get());
    }

    /**
     * Test extract domain should return user.
     */
    @Test
    void testExtractDomain_ShouldReturnUser() {
        // Extract domain from the generated token
        Optional<String> extractedUser = jwtService.extractUserName(token, key);

        assertTrue(extractedUser.isPresent());
        assertEquals(claimsMap.get(JwtConstants.JWT_SENDER_USER), extractedUser.get());
    }

    /**
     * Test extract domain should return is admin.
     */
    @Test
    void testExtractDomain_ShouldReturnIsAdmin() {
        // Extract domain from the generated token
        Optional<Boolean> extractedIsAdmin = jwtService.extractIsAdmin(token, key);

        assertTrue(extractedIsAdmin.isPresent());
        assertEquals(claimsMap.get(JwtConstants.JWT_IS_ADMIN), extractedIsAdmin.get());
    }

    /**
     * Test extract domain should return logging application.
     */
    @Test
    void testExtractDomain_ShouldReturnLoggingApplication() {
        // Extract domain from the generated token
        Optional<String> extractedLogApp = jwtService.extractApplication(token, key);

        assertTrue(extractedLogApp.isPresent());
        assertEquals(claimsMap.get(JwtConstants.JWT_LOG_APP), extractedLogApp.get());
    }

    /**
     * Test extract domain should return account type.
     */
    @Test
    void testExtractDomain_ShouldReturnAccountType() {
        // Extract domain from the generated token
        Optional<String> extractedAccountType = jwtService.extractAccountType(token, key);

        assertTrue(extractedAccountType.isPresent());
        assertEquals(claimsMap.get(JwtConstants.JWT_SENDER_ACCOUNT_TYPE), extractedAccountType.get());
    }
}