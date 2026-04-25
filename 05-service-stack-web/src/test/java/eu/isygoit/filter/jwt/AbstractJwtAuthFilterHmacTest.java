package eu.isygoit.filter.jwt;

import eu.isygoit.helper.HmacHelper;
import eu.isygoit.jwt.IJwtService;
import eu.isygoit.service.RequestContextService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AbstractJwtAuthFilterHmacTest {

    private final String testSecret = "my-secret-key-for-hmac-validation-12345";

    @Mock
    private IJwtService jwtService;
    @Mock
    private RequestContextService requestContextService;

    @Mock
    private HttpServletRequest request;
    private TestJwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new TestJwtAuthFilter(jwtService, requestContextService);
        ReflectionTestUtils.setField(filter, "shouldNotFilterKey", testSecret);
    }

    @Test
    void testShouldNotFilter_PublicUri_ReturnsTrue() {
        when(request.getRequestURI()).thenReturn("/api/v1/public/something");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void testShouldNotFilter_ValidHmac_ReturnsTrue() {
        String message = "test-request-123";
        String signature = HmacHelper.generateHmac(message, testSecret);

        when(request.getRequestURI()).thenReturn("/api/v1/private/something");
        when(request.getHeader("X-Auth-Message")).thenReturn(message);
        when(request.getHeader("X-Auth-Signature")).thenReturn(signature);

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void testShouldNotFilter_InvalidHmac_ReturnsFalse() {
        String message = "test-request-123";
        String signature = "invalid-signature";

        when(request.getRequestURI()).thenReturn("/api/v1/private/something");
        when(request.getHeader("X-Auth-Message")).thenReturn(message);
        when(request.getHeader("X-Auth-Signature")).thenReturn(signature);

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void testShouldNotFilter_MissingHeaders_ReturnsFalse() {
        when(request.getRequestURI()).thenReturn("/api/v1/private/something");
        when(request.getHeader("X-Auth-Message")).thenReturn(null);
        when(request.getHeader("X-Auth-Signature")).thenReturn(null);

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void testShouldNotFilter_OldStaticKey_ReturnsFalse() {
        when(request.getRequestURI()).thenReturn("/api/v1/private/something");
        when(request.getHeader("SHOULD_NOT_FILTER_KEY")).thenReturn(testSecret);

        assertFalse(filter.shouldNotFilter(request));
    }

    // A concrete subclass for testing purposes
    private static class TestJwtAuthFilter extends AbstractJwtAuthFilter {
        public TestJwtAuthFilter(IJwtService jwtService, RequestContextService requestContextService) {
            super(jwtService, requestContextService);
        }

        @Override
        public boolean isTokenValid(String jwt, String tenant, String application, String userName) {
            return true;
        }

        @Override
        public void addAttributes(HttpServletRequest request, Map<String, Object> attributes) {
            // No-op for test
        }
    }
}
