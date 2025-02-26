package eu.isygoit.filter;

import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.enums.IEnumAppToken;
import eu.isygoit.exception.TokenInvalidException;
import eu.isygoit.service.TokenServiceApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * JWT KMS Client authentication filter that validates tokens using a remote TokenServiceApi.
 * This abstract implementation extends AbstractJwtAuthFilter and provides client-side
 * token validation through a Feign client.
 */
@Slf4j
public abstract class JwtKmsClientAuthFilter extends AbstractJwtAuthFilter {

    // Constant empty context to avoid object creation on each validation
    private static final RequestContextDto EMPTY_CONTEXT = RequestContextDto.builder().build();

    // Optional: Consider adding circuit breaker pattern here
    // private final CircuitBreaker circuitBreaker;

    @Autowired
    private TokenServiceApi tokenService;

    /**
     * Validates a JWT token using the remote token service API.
     * Makes a Feign client call to the token service and handles the response.
     *
     * @param jwt         the JWT token to validate
     * @param domain      the domain extracted from token
     * @param application the application identifier from token
     * @param userName    the username from token
     * @return true if token is valid
     * @throws TokenInvalidException if token validation fails or service is unavailable
     */
    @Override
    public boolean isTokenValid(String jwt, String domain, String application, String userName) {
        if (tokenService == null) {
            log.error("Token validation failed: TokenServiceApi is not available");
            throw new TokenInvalidException("No token validator available");
        }

        // Create user identifier by combining lowercase username with domain
        // Do this once to avoid recreating the string multiple times
        String userIdentifier = userName.toLowerCase() + "@" + domain;

        try {
            long startTime = System.nanoTime();

            // Use the constant empty context to avoid object creation
            ResponseEntity<Boolean> result = tokenService.isTokenValid(
                    EMPTY_CONTEXT,
                    domain,
                    application,
                    IEnumAppToken.Types.ACCESS,
                    jwt,
                    userIdentifier
            );

            // Log API call duration for performance monitoring
            if (log.isDebugEnabled()) {
                long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
                log.debug("Token validation call completed in {} ms for user: {}", duration, userIdentifier);
            }

            // Check for valid response and body
            if (!result.getStatusCode().is2xxSuccessful() ||
                    !result.hasBody() ||
                    Boolean.FALSE.equals(result.getBody())) {

                log.warn("Token validation failed for user: {}, application: {}, domain: {}, status: {}",
                        userName, application, domain,
                        result.getStatusCode());

                throw new TokenInvalidException("KMS::isTokenValid");
            }

            // Log successful validation
            if (log.isDebugEnabled()) {
                log.debug("Token successfully validated for user: {}, application: {}", userName, application);
            }

        } catch (TokenInvalidException e) {
            // Rethrow TokenInvalidException without wrapping
            throw e;
        } catch (Exception e) {
            // Log the exception but allow the request to proceed
            // Consider if this is the desired behavior or if you should throw an exception
            log.error("Remote token service call failed for user: {}, application: {}, domain: {}",
                    userName, application, domain, e);

            // Uncomment to throw exception instead of continuing
            // throw new TokenInvalidException("Token validation service unavailable", e);
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
            // Use direct method reference for better performance
            attributes.forEach(request::setAttribute);
        }
    }

    /**
     * Gets the token service API instance.
     * Protected access allows subclasses to access the token service if needed.
     *
     * @return the token service API
     */
    protected TokenServiceApi getTokenService() {
        return tokenService;
    }
}