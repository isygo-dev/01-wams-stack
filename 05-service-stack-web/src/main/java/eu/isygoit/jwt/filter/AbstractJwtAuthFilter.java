package eu.isygoit.jwt.filter;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base JWT authentication filter that validates tokens and establishes security context.
 * This abstract class provides core JWT processing functionality while allowing
 * specific token validation logic to be implemented by subclasses.
 */
@Slf4j
public abstract class AbstractJwtAuthFilter extends OncePerRequestFilter {

    // Constants for frequently checked URI prefixes/patterns to avoid string allocations
    private static final String PUBLIC_API_PREFIX = "/api/v1/public";
    private static final String IMAGE_DOWNLOAD_PATTERN = "/image/download";
    private static final String FILE_DOWNLOAD_PATTERN = "/file/download";

    // Cache for request URIs that should not be filtered (thread-safe)
    private static final Map<String, Boolean> URI_FILTER_CACHE = new ConcurrentHashMap<>();

    // Default RequestContextDto for unauthenticated requests (immutable singleton)
    private static final RequestContextDto DEFAULT_CONTEXT = RequestContextDto.builder()
            .senderDomain(DomainConstants.SUPER_DOMAIN_NAME)
            .senderUser("root")
            .isAdmin(true)
            .logApp("application")
            .build();

    // Predefined attribute map to avoid allocation in the error path
    private static final Map<String, Object> DEFAULT_ATTRIBUTES =
            Map.of(JwtConstants.JWT_USER_CONTEXT, DEFAULT_CONTEXT);

    @Value("${app.feign.shouldNotFilterKey}")
    private String shouldNotFilterKey;

    @Autowired
    private IJwtService jwtService;

    /**
     * Validates if the JWT token is valid for the given context.
     *
     * @param jwt         the JWT token string
     * @param domain      the domain extracted from token
     * @param application the application identifier from token
     * @param userName    the username from token
     * @return true if token is valid, false otherwise
     */
    public abstract boolean isTokenValid(String jwt, String domain, String application, String userName);

    /**
     * Adds custom attributes to the request.
     * Implementations should use this to add context-specific attributes.
     *
     * @param request    the HTTP request
     * @param attributes map of attributes to add to the request
     */
    public abstract void addAttributes(HttpServletRequest request, Map<String, Object> attributes);

    @Override
    public boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // Check cache first to avoid string operations for frequent requests
        return URI_FILTER_CACHE.computeIfAbsent(uri, this::shouldSkipUri) ||
                shouldNotFilterKey.equals(request.getHeader("SHOULD_NOT_FILTER_KEY"));
    }

    /**
     * Determines if a URI should be skipped for filtering.
     * Extracted for better performance via caching.
     *
     * @param uri the request URI to check
     * @return true if the URI should be skipped
     */
    private boolean shouldSkipUri(String uri) {
        return uri.startsWith(PUBLIC_API_PREFIX) ||
                uri.contains(IMAGE_DOWNLOAD_PATTERN) ||
                uri.contains(FILE_DOWNLOAD_PATTERN);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Get JWT token from request
        String jwt = UrlHelper.getJwtTokenFromRequest(request);

        if (StringUtils.hasText(jwt)) {
            log.debug("Processing request: {} - URI: {}", request.getMethod(), request.getRequestURI());

            try {
                // Extract all needed claims at once to minimize JWT parsing overhead
                processAuthenticatedRequest(request, response, filterChain, jwt);
            } catch (JwtException | IllegalArgumentException | TokenInvalidException e) {
                // Log error and set unauthorized response
                log.error("Invalid token for request: {} {}, Error: {}",
                        request.getMethod(), request.getRequestURI(), e.getMessage());

                handleInvalidToken(response);
            }
        } else {
            // Process request without authentication
            handleMissingToken(request, response, filterChain);
        }
    }

    /**
     * Processes a request with a valid JWT token.
     * Extracted to separate method to improve readability.
     */
    private void processAuthenticatedRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            String jwt) throws ServletException, IOException {

        // Extract all needed claims at once
        String subject = jwtService.extractSubject(jwt).orElseThrow();
        String userName = jwtService.extractUserName(jwt).orElseThrow();
        String application = jwtService.extractApplication(jwt).orElseThrow();
        String domain = jwtService.extractDomain(jwt).orElseThrow();
        Boolean isAdmin = jwtService.extractIsAdmin(jwt);

        // Validate token
        isTokenValid(jwt, domain, application, userName);

        // Set security context
        setSecurityContext(subject, isAdmin);

        // Add context attributes to request
        RequestContextDto contextDto = buildRequestContext(domain, userName, isAdmin, application);
        addAttributes(request, Map.of(JwtConstants.JWT_USER_CONTEXT, contextDto));

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Sets the security context with authentication details.
     */
    private void setSecurityContext(String subject, Boolean isAdmin) {
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .username(subject)
                .isAdmin(isAdmin)
                .password("password") // Consider using a more secure approach
                .passwordExpired(false)
                .domainEnabled(true)
                .accountEnabled(true)
                .accountExpired(true)
                .accountLocked(true)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(new CustomAuthentification(
                        userDetails,
                        "password",
                        Collections.emptyList())); // Use empty immutable list for better performance
    }

    /**
     * Builds a RequestContextDto from token claims.
     */
    private RequestContextDto buildRequestContext(String domain, String userName, Boolean isAdmin, String application) {
        return RequestContextDto.builder()
                .senderDomain(domain)
                .senderUser(userName)
                .isAdmin(isAdmin)
                .logApp(application)
                .build();
    }

    /**
     * Handles case where token is invalid.
     */
    private void handleInvalidToken(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
    }

    /**
     * Handles case where token is missing.
     * Uses default attributes for unauthenticated requests.
     */
    private void handleMissingToken(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (log.isDebugEnabled()) {
            log.debug("Missing token for request: {} {}", request.getMethod(), request.getRequestURI());
        }

        // Add default context attributes
        addAttributes(request, DEFAULT_ATTRIBUTES);

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}