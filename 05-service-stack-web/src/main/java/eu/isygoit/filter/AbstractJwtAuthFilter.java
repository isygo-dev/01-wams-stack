package eu.isygoit.filter;

import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.TokenInvalidException;
import eu.isygoit.helper.UrlHelper;
import eu.isygoit.jwt.IJwtService;
import eu.isygoit.security.CustomAuthentification;
import eu.isygoit.security.CustomUserDetails;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

/**
 * The type Abstract jwt auth filter.
 * <p>
 * Inherited services should autowire:
 * private IJwtService jwtService;
 */
@Slf4j
public abstract class AbstractJwtAuthFilter extends OncePerRequestFilter {

    @Value("${app.feign.shouldNotFilterKey}")
    private String shouldNotFilter;

    /**
     * Gets jwt service instance.
     *
     * @return the jwt service instance
     */
    protected abstract IJwtService getJwtServiceInstance();

    /**
     * Gets token service.
     *
     * @return the token service
     */
    protected Optional<IJwtService> getJwtServiceService() {
        return Optional.ofNullable(getJwtServiceInstance());
    }

    /**
     * Is token valid boolean.
     *
     * @param jwt         the jwt
     * @param domain      the domain
     * @param application the application
     * @param userName    the user name
     * @return the boolean
     */
    public abstract boolean isTokenValid(String jwt, String domain, String application, String userName);

    /**
     * Add attributes.
     *
     * @param request    the request
     * @param attributes the attributes
     */
    public abstract void addAttributes(HttpServletRequest request, Map<String, Object> attributes);

    @Override
    public boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Check if the request should not be filtered based on URI or header flag
        log.info("Jwt auth filter: attribute SHOULD_NOT_FILTER_KEY: {}", request.getHeader("SHOULD_NOT_FILTER_KEY"));
        return isExcludedUri(request) || shouldNotFilter.equals(request.getHeader("SHOULD_NOT_FILTER_KEY"));
    }

    private boolean isExcludedUri(HttpServletRequest request) {
        // Check if the URI is excluded from filtering, such as public endpoints or file downloads
        String uri = request.getRequestURI();
        return uri.startsWith("/api/v1/public")
                || uri.contains("/image/download")
                || uri.contains("/file/download");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Extract JWT from the request
        String jwt = UrlHelper.getJwtFromRequest(request);

        // Proceed with token validation if JWT is present
        if (StringUtils.hasText(jwt)) {
            log.info("Request received:  {} - {} - {}", request.getMethod(), request.getAuthType(), request.getRequestURI());
            try {
                handleJwtAuthentication(jwt, request);  // Handle the JWT validation and authentication
                filterChain.doFilter(request, response);  // Continue processing the request
            } catch (JwtException | IllegalArgumentException | TokenInvalidException e) {
                // Handle invalid token exceptions by responding with an UNAUTHORIZED error
                log.error("<Error>: Invalid token: {} > {} / {}", request.getMethod(), request.getRequestURI(), e);
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            }
        } else {
            // Log the error when no token is found and set default attributes
            log.error("<Error>: Missed token for request {} > {}", request.getMethod(), request.getRequestURI());
            setDefaultAttributes(request);
            filterChain.doFilter(request, response);  // Continue processing the request without authentication
        }
    }

    private void handleJwtAuthentication(String jwt, HttpServletRequest request) {
        // Retrieve JWT service instance and handle JWT extraction
        var jwtService = getJwtServiceService()
                .orElseThrow(() -> new TokenInvalidException("JWT Service not available"));

        var subject = jwtService.extractSubject(jwt);
        var userName = jwtService.extractUserName(jwt);
        var application = jwtService.extractApplication(jwt);
        var domain = jwtService.extractDomain(jwt);
        var isAdmin = jwtService.extractIsAdmin(jwt);

        // Use ifPresentOrElse to handle presence/absence of values in a concise manner
        subject.ifPresentOrElse(value -> {
            userName.ifPresentOrElse(name -> {
                        if (!isTokenValid(jwt, domain.orElse("NA"), application.orElse("NA"), name)) {
                            throw new TokenInvalidException("Invalid JWT/userName");
                        }
                    },
                    () -> {
                        throw new TokenInvalidException("Invalid JWT/userName");
                    });

            // Set authentication details once the token is validated
            setAuthentication(value, isAdmin.orElse(Boolean.FALSE));
        }, () -> {
            throw new TokenInvalidException("Invalid JWT/subject");
        });

        // Add attributes to the request, including user and domain details
        addAttributes(request, createRequestAttributes(domain, userName, isAdmin.orElse(Boolean.FALSE), application));
    }

    private void setAuthentication(String subject, Boolean isAdmin) {
        // Create user details and set authentication in the security context
        var userDetails = CustomUserDetails.builder()
                .username(subject)
                .isAdmin(isAdmin)
                .password("password")
                .passwordExpired(false)
                .domainEnabled(true)
                .accountEnabled(true)
                .accountExpired(true)
                .accountLocked(true)
                .build();

        Authentication authentication = new CustomAuthentification(userDetails, "password", new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Map<String, Object> createRequestAttributes(Optional<String> domain, Optional<String> userName,
                                                        Boolean isAdmin, Optional<String> application) {
        // Create and return a map of attributes to be added to the request, including user details
        return Map.of(JwtConstants.JWT_USER_CONTEXT, RequestContextDto.builder()
                .senderDomain(domain.orElse("NA"))
                .senderUser(userName.orElse("NA"))
                .isAdmin(isAdmin)
                .logApp(application.orElse("NA"))
                .build());
    }

    private void setDefaultAttributes(HttpServletRequest request) {
        // Set default attributes (used when no token is provided, e.g., for the "root" user)
        addAttributes(request, Map.of(JwtConstants.JWT_USER_CONTEXT, RequestContextDto.builder()
                .senderDomain(DomainConstants.SUPER_DOMAIN_NAME)
                .senderUser("root")
                .isAdmin(true)
                .logApp("application")
                .build()));
    }
}