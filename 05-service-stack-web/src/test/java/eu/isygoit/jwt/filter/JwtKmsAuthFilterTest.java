package eu.isygoit.jwt.filter;

import eu.isygoit.enums.IEnumToken;
import eu.isygoit.exception.TokenInvalidException;
import eu.isygoit.service.ITokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * The type Jwt kms auth filter test.
 */
class JwtKmsAuthFilterTest {

    @InjectMocks
    private JwtKmsAuthFilter jwtKmsAuthFilter;

    @Mock
    private ITokenService tokenService;

    @Mock
    private HttpServletRequest httpServletRequest;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test add not filter host and should not filter host.
     */
    @Test
    void testAddNotFilterHostAndShouldNotFilterHost() {
        String host = "example.com";
        String value = "api";

        assertFalse(JwtKmsAuthFilter.shouldNotFilterHost(host),
                "Host should not be in the list initially");

        JwtKmsAuthFilter.addNotFilterHost(host, value);

        assertTrue(JwtKmsAuthFilter.shouldNotFilterHost(host),
                "Host should be present after adding it");
    }

    /**
     * Test is token valid valid token returns true.
     */
    @Test
    void testIsTokenValid_ValidToken_ReturnsTrue() {
        String jwt = "valid.jwt.token";
        String domain = "domain.com";
        String application = "myApp";
        String userName = "JohnDoe";
        String expectedUserId = "johndoe@domain.com";

        when(tokenService.isTokenValid(domain, application, IEnumToken.Types.ACCESS, jwt, expectedUserId))
                .thenReturn(true);

        boolean result = jwtKmsAuthFilter.isTokenValid(jwt, domain, application, userName);

        assertTrue(result, "Token should be valid");
        verify(tokenService).isTokenValid(domain, application, IEnumToken.Types.ACCESS, jwt, expectedUserId);
    }

    /**
     * Test is token valid invalid token throws token invalid exception.
     */
    @Test
    void testIsTokenValid_InvalidToken_ThrowsTokenInvalidException() {
        String jwt = "invalid.jwt.token";
        String domain = "test.org";
        String application = "testApp";
        String userName = "Alice";
        String expectedUserId = "alice@test.org";

        when(tokenService.isTokenValid(domain, application, IEnumToken.Types.ACCESS, jwt, expectedUserId))
                .thenReturn(false);

        TokenInvalidException ex = assertThrows(TokenInvalidException.class, () ->
                jwtKmsAuthFilter.isTokenValid(jwt, domain, application, userName));

        assertEquals("KMS::isTokenValid", ex.getMessage());
        verify(tokenService).isTokenValid(domain, application, IEnumToken.Types.ACCESS, jwt, expectedUserId);
    }

    /**
     * Test add attributes empty map does not call set attribute.
     */
    @Test
    void testAddAttributes_EmptyMap_DoesNotCallSetAttribute() {
        Map<String, Object> attributes = new HashMap<>();

        jwtKmsAuthFilter.addAttributes(httpServletRequest, attributes);

        verify(httpServletRequest, never()).setAttribute(anyString(), any());
    }

    /**
     * Test add attributes null map does not throw or set.
     */
    @Test
    void testAddAttributes_NullMap_DoesNotThrowOrSet() {
        jwtKmsAuthFilter.addAttributes(httpServletRequest, null);

        verify(httpServletRequest, never()).setAttribute(anyString(), any());
    }

    /**
     * Test add attributes with values adds all to request.
     */
    @Test
    void testAddAttributes_WithValues_AddsAllToRequest() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 101);
        attributes.put("role", "admin");

        jwtKmsAuthFilter.addAttributes(httpServletRequest, attributes);

        verify(httpServletRequest).setAttribute("userId", 101);
        verify(httpServletRequest).setAttribute("role", "admin");
    }

    /**
     * Test should not filter host empty map returns false.
     */
    @Test
    void testShouldNotFilterHost_EmptyMap_ReturnsFalse() {
        assertFalse(JwtKmsAuthFilter.shouldNotFilterHost("not-present.com"));
    }
}
