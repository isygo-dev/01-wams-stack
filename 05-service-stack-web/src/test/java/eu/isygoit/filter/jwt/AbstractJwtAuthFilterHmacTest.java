package eu.isygoit.filter.jwt;

import eu.isygoit.jwt.IJwtService;
import eu.isygoit.jwt.filter.AbstractJwtAuthFilter;
import eu.isygoit.service.RequestContextService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Set;

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

    // A concrete subclass for testing purposes
    private static class TestJwtAuthFilter extends AbstractJwtAuthFilter {
        public TestJwtAuthFilter(IJwtService jwtService, RequestContextService requestContextService) {
            super(jwtService, requestContextService);
        }

        @Override
        public boolean isTokenValid(String jwt, String tenant, Set<String> audience, String userName) {
            return true;
        }

        @Override
        public void addAttributes(HttpServletRequest request, Map<String, Object> attributes) {
            // No-op for test
        }
    }
}
