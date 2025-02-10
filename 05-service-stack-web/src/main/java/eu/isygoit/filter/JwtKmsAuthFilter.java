package eu.isygoit.filter;

import eu.isygoit.enums.IEnumAppToken;
import eu.isygoit.exception.TokenInvalidException;
import eu.isygoit.service.token.ITokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract class JwtKmsAuthFilter, which extends AbstractJwtAuthFilter.
 * This class provides base functionality for handling JWT authentication with KMS.
 * It requires the implementation of getTokenService() to obtain a specific token service instance wrapped in an Optional.
 * <p>
 * Inherited services should autowire:
 * private ITokenService tokenService;
 */
@Slf4j
public abstract class JwtKmsAuthFilter extends AbstractJwtAuthFilter {

    /**
     * A map that holds hosts which should not be filtered by the JWT authentication filter.
     * This is a static map, shared across all instances of this class.
     */
    public static Map<String, String> shouldNotFilterHosts = new HashMap<>();

    /**
     * Gets repository instance.
     *
     * @return the repository instance
     */
    protected abstract ITokenService getTokenServiceInstance();

    /**
     * Gets token service.
     *
     * @return the token service
     */
    protected Optional<ITokenService> getTokenService() {
        return Optional.ofNullable(getTokenServiceInstance());
    }

    /**
     * Validates the provided JWT token using the token service.
     * If the token is invalid or if no token service is available, it throws a TokenInvalidException.
     *
     * @param jwt         the JWT token to be validated
     * @param domain      the domain of the application
     * @param application the application name
     * @param userName    the user name associated with the token
     * @return true if the token is valid
     * @throws TokenInvalidException if the token is invalid or the token service is unavailable
     */
    @Override
    public boolean isTokenValid(String jwt, String domain, String application, String userName) {
        // Use the Optional's ifPresentOrElse method to handle the case where the token service is present or not
        getTokenService().ifPresentOrElse(tokenService -> {
            // Validate the token using the token service
            if (!tokenService.isTokenValid(domain, application, IEnumAppToken.Types.ACCESS, jwt,
                    userName.toLowerCase() + "@" + domain)) {
                throw new TokenInvalidException("KMS::isTokenValid");
            }
        }, () -> {
            // If the token service is absent, throw an exception
            throw new TokenInvalidException("No token validator available");
        });
        return true;
    }

    /**
     * Adds attributes to the HttpServletRequest if the provided attributes map is not empty.
     * This is useful for adding dynamic attributes to the request during the filter's execution.
     *
     * @param request    the HTTP request to which attributes should be added
     * @param attributes the map of attributes to add to the request
     */
    @Override
    public void addAttributes(HttpServletRequest request, Map<String, Object> attributes) {
        if (!CollectionUtils.isEmpty(attributes)) {
            // Iterate over the attributes map and set each attribute on the request
            attributes.forEach((s, o) -> {
                request.setAttribute(s, o);
            });
        }
    }
}