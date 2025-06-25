package eu.isygoit.filter.jwt;

import eu.isygoit.enums.IEnumToken;
import eu.isygoit.exception.TokenInvalidException;
import eu.isygoit.service.ITokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT KMS authentication filter that validates tokens using the token service.
 * This implementation extends AbstractJwtAuthFilter and adds KMS-specific validation logic.
 * It manages a list of hosts that should not be filtered and handles token validation
 * through the ITokenService.
 */
@Slf4j
public class JwtKmsAuthFilter extends AbstractJwtAuthFilter {

    /**
     * Thread-safe map of hosts that should not be filtered.
     * Using ConcurrentHashMap for better performance in concurrent environments.
     */
    private static final Map<String, String> shouldNotFilterHosts = new ConcurrentHashMap<>();

    @Autowired
    private ITokenService tokenService;

    /**
     * Adds a host to the list of hosts that should not be filtered.
     *
     * @param host  the host name or pattern
     * @param value the associated value
     */
    public static void addNotFilterHost(String host, String value) {
        shouldNotFilterHosts.put(host, value);
    }

    /**
     * Checks if a host should not be filtered.
     *
     * @param host the host to check
     * @return true if the host should not be filtered
     */
    public static boolean shouldNotFilterHost(String host) {
        return shouldNotFilterHosts.containsKey(host);
    }

    /**
     * Validates a JWT token using the token service.
     * Creates a user identifier by combining username and tenant.
     *
     * @param jwt         the JWT token to validate
     * @param tenant      the tenant extracted from token
     * @param application the application identifier from token
     * @param userName    the username from token
     * @return true if token is valid
     * @throws TokenInvalidException if token is invalid or token service is unavailable
     */
    @Override
    public boolean isTokenValid(String jwt, String tenant, String application, String userName) {
        if (tokenService == null) {
            log.error("Token validation failed: Token service is not available");
            throw new TokenInvalidException("No token validator available");
        }

        // Create user identifier by combining lowercase username with tenant
        String userIdentifier = userName.toLowerCase() + "@" + tenant;

        // Validate token using token service
        if (!tokenService.isTokenValid(tenant, application, IEnumToken.Types.ACCESS, jwt, userIdentifier)) {
            log.warn("Invalid token for user: {}, application: {}, tenant: {}",
                    userName, application, tenant);
            throw new TokenInvalidException("KMS::isTokenValid");
        }

        if (log.isDebugEnabled()) {
            log.debug("Token successfully validated for user: {}, application: {}, tenant: {}",
                    userName, application, tenant);
        }

        return true;
    }

    /**
     * Adds attributes to the request.
     * Efficiently adds all provided attributes to the request attributes.
     *
     * @param request    the HTTP request to add attributes to
     * @param attributes map of attributes to add
     */
    @Override
    public void addAttributes(HttpServletRequest request, Map<String, Object> attributes) {
        if (!CollectionUtils.isEmpty(attributes)) {
            // Use direct forEach for better performance instead of lambda
            attributes.forEach(request::setAttribute);
        }
    }
}