package eu.isygoit.filter.jwt;

import eu.isygoit.enums.IEnumToken;
import eu.isygoit.exception.TokenInvalidException;
import eu.isygoit.service.ITokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtKmsAuthFilter Tests")
class JwtKmsAuthFilterTest {

    @Mock
    private ITokenService tokenService;

    @InjectMocks
    private JwtKmsAuthFilter jwtKmsAuthFilter;

    @BeforeEach
    void setUp() {
        // Clear static map if possible, but it's private. 
        // We can just test its functionality.
    }

    @Test
    @DisplayName("isTokenValid should return true when tokenService validates token")
    void testIsTokenValid_Success() {
        String jwt = "valid-token";
        String tenant = "tenant1";
        String application = "app1";
        String userName = "user1";
        String userIdentifier = "user1@tenant1";

        when(tokenService.isTokenValid(tenant, application, IEnumToken.Types.ACCESS, jwt, userIdentifier)).thenReturn(true);

        assertTrue(jwtKmsAuthFilter.isTokenValid(jwt, tenant, application, userName));
    }

    @Test
    @DisplayName("isTokenValid should throw TokenInvalidException when tokenService returns false")
    void testIsTokenValid_Failure() {
        String jwt = "invalid-token";
        String tenant = "tenant1";
        String application = "app1";
        String userName = "user1";
        String userIdentifier = "user1@tenant1";

        when(tokenService.isTokenValid(tenant, application, IEnumToken.Types.ACCESS, jwt, userIdentifier)).thenReturn(false);

        assertThrows(TokenInvalidException.class, () -> jwtKmsAuthFilter.isTokenValid(jwt, tenant, application, userName));
    }

    @Test
    @DisplayName("isTokenValid should throw TokenInvalidException when tokenService is null")
    void testIsTokenValid_TokenServiceNull() {
        JwtKmsAuthFilter filterWithoutService = new JwtKmsAuthFilter();
        assertThrows(TokenInvalidException.class, () -> filterWithoutService.isTokenValid("jwt", "tenant", "app", "user"));
    }

    @Test
    @DisplayName("addNotFilterHost and shouldNotFilterHost should manage host list correctly")
    void testHostFiltering() {
        String host = "example1.com";
        JwtKmsAuthFilter.addNotFilterHost(host, "some-value");
        assertTrue(JwtKmsAuthFilter.shouldNotFilterHost(host));
        assertFalse(JwtKmsAuthFilter.shouldNotFilterHost("other.com"));
    }

    @Test
    @DisplayName("addAttributes should add attributes to request")
    void testAddAttributes() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, Object> attributes = Map.of("attr1", "val1", "attr2", 123);

        jwtKmsAuthFilter.addAttributes(request, attributes);

        verify(request).setAttribute("attr1", "val1");
        verify(request).setAttribute("attr2", 123);
    }
}
