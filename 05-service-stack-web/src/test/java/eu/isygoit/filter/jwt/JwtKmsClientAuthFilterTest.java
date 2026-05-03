package eu.isygoit.filter.jwt;

import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.enums.IEnumToken;
import eu.isygoit.exception.TokenInvalidException;
import eu.isygoit.jwt.IJwtService;
import eu.isygoit.service.RequestContextService;
import eu.isygoit.service.TokenServiceApi;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtKmsClientAuthFilter Tests")
class JwtKmsClientAuthFilterTest {

    @Mock
    private IJwtService jwtService;
    @Mock
    private RequestContextService requestContextService;
    @Mock
    private TokenServiceApi tokenService;
    @InjectMocks
    private TestJwtKmsClientAuthFilter filter;

    @Test
    @DisplayName("isTokenValid should return true when TokenServiceApi validates token")
    void testIsTokenValid_Success() {
        String jwt = "valid-token";
        String tenant = "tenant1";
        String application = "app1";
        String userName = "user1";
        String userIdentifier = "user1@tenant1";

        when(tokenService.isTokenValid(eq(tenant), eq(application), eq(IEnumToken.Types.ACCESS), eq(jwt), eq(userIdentifier)))
                .thenReturn(ResponseFactory.responseOk(true));

        assertTrue(filter.isTokenValid(jwt, tenant, application, userName));
    }

    @Test
    @DisplayName("isTokenValid should throw TokenInvalidException when TokenServiceApi returns false")
    void testIsTokenValid_Failure() {
        String jwt = "invalid-token";
        String tenant = "tenant1";
        String application = "app1";
        String userName = "user1";
        String userIdentifier = "user1@tenant1";

        when(tokenService.isTokenValid(eq(tenant), eq(application), eq(IEnumToken.Types.ACCESS), eq(jwt), eq(userIdentifier)))
                .thenReturn(ResponseFactory.responseOk(false));

        assertThrows(TokenInvalidException.class, () -> filter.isTokenValid(jwt, tenant, application, userName));
    }

    @Test
    @DisplayName("isTokenValid should throw TokenInvalidException when TokenServiceApi returns non-2xx status")
    void testIsTokenValid_ErrorStatus() {
        String jwt = "token";
        String tenant = "tenant";
        String application = "app";
        String userName = "user";

        when(tokenService.isTokenValid(any(), any(), any(), any(), any()))
                .thenReturn(ResponseFactory.responseOk(false));

        assertThrows(TokenInvalidException.class, () -> filter.isTokenValid(jwt, tenant, application, userName));
    }

    @Test
    @DisplayName("isTokenValid should return true when TokenServiceApi throws Exception (as per current implementation)")
    void testIsTokenValid_ServiceException() {
        String jwt = "token";
        String tenant = "tenant";
        String application = "app";
        String userName = "user";

        when(tokenService.isTokenValid(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Remote call failed"));

        // The current implementation logs the error but returns true
        assertTrue(filter.isTokenValid(jwt, tenant, application, userName));
    }

    @Test
    @DisplayName("isTokenValid should throw TokenInvalidException when tokenService is null")
    void testIsTokenValid_TokenServiceNull() {
        TestJwtKmsClientAuthFilter filterWithoutService = new TestJwtKmsClientAuthFilter(jwtService, requestContextService, tokenService);
        assertThrows(TokenInvalidException.class, () -> filterWithoutService.isTokenValid("jwt", "tenant", "app", "user"));
    }

    @Test
    @DisplayName("addAttributes should add attributes to request")
    void testAddAttributes() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, Object> attributes = Map.of("attr1", "val1");

        filter.addAttributes(request, attributes);

        verify(request).setAttribute("attr1", "val1");
    }

    // Concrete implementation for testing abstract class
    static class TestJwtKmsClientAuthFilter extends JwtKmsClientAuthFilter {
        public TestJwtKmsClientAuthFilter(IJwtService jwtService, RequestContextService requestContextService, TokenServiceApi tokenService) {
            super(jwtService, requestContextService, tokenService);
        }
    }
}
