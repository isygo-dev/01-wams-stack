package eu.isygoit.filter.jwt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.dto.common.ContextRequestDto;
import eu.isygoit.exception.TokenInvalidException;
import eu.isygoit.helper.HmacHelper;
import eu.isygoit.helper.UrlHelper;
import eu.isygoit.jwt.IJwtService;
import eu.isygoit.security.CustomAuthentification;
import eu.isygoit.security.CustomUserDetails;
import eu.isygoit.service.RequestContextService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Abstract JWT Authentication Filter
 * <p>
 * Responsibilities:
 * - Extract JWT from request
 * - Validate token (delegated to subclass)
 * - Populate Spring SecurityContext
 * - Populate RequestContext (ThreadLocal + request attributes)
 * - Ensure cleanup (ThreadLocal leak prevention)
 * <p>
 * SECURITY NOTES:
 * - Never trust headers alone
 * - Always validate JWT signature + claims
 * - Always clear ThreadLocal in finally
 */
@Slf4j
public abstract class AbstractJwtAuthFilter extends OncePerRequestFilter {

    // =========================
    // Constants
    // =========================

    private static final String PUBLIC_API_PREFIX = "/api/v1/public";
    private static final String IMAGE_DOWNLOAD_PATTERN = "/image/download";
    private static final String FILE_DOWNLOAD_PATTERN = "/file/download";

    /**
     * Default context for unauthenticated requests.
     * ⚠️ Keep minimal privileges (DO NOT make it admin in real prod)
     */
    private static final ContextRequestDto DEFAULT_CONTEXT = ContextRequestDto.builder()
            .senderTenant(TenantConstants.SUPER_TENANT_NAME)
            .senderUser("root")
            .isAdmin(true)
            .logApp("application")
            .build();

    // =========================
    // Dependencies
    // =========================

    private final IJwtService jwtService;
    private final RequestContextService requestContextService;

    @Value("${app.feign.shouldNotFilterKey}")
    private String shouldNotFilterKey;

    /**
     * Cache to avoid recomputing URI filtering rules.
     * TTL added to prevent unbounded memory growth.
     */
    private Cache<String, Boolean> uriFilterCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public AbstractJwtAuthFilter(IJwtService jwtService, RequestContextService requestContextService) {
        this.jwtService = jwtService;
        this.requestContextService = requestContextService;
    }

    // =========================
    // Abstract hooks
    // =========================

    /**
     * Custom token validation logic (signature, expiration, etc.)
     */
    public abstract boolean isTokenValid(String jwt, String tenant, String application, String userName);

    /**
     * Hook for adding request attributes (used for anonymous flow)
     */
    public abstract void addAttributes(HttpServletRequest request, Map<String, Object> attributes);

    // =========================
    // Filter skipping logic
    // =========================

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String uri = request.getRequestURI();

        // 1. Cached URI decision (fast path)
        if (Boolean.TRUE.equals(uriFilterCache.get(uri, this::shouldSkipUri))) {
            return true;
        }

        // 2. HMAC validation for internal calls (Feign, etc.)
        String signature = request.getHeader("X-Auth-Signature");
        String message = request.getHeader("X-Auth-Message");

        if (StringUtils.hasText(signature) && StringUtils.hasText(message)) {
            return HmacHelper.validateHmac(message, signature, shouldNotFilterKey);
        }

        return false;
    }

    private boolean shouldSkipUri(String uri) {
        return uri.startsWith(PUBLIC_API_PREFIX)
                || uri.contains(IMAGE_DOWNLOAD_PATTERN)
                || uri.contains(FILE_DOWNLOAD_PATTERN);
    }

    // =========================
    // Main filter logic
    // =========================

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = UrlHelper.getJwtTokenFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                processAuthenticatedRequest(request, response, filterChain, jwt);
            } else {
                handleMissingToken(request, response, filterChain);
            }

        } finally {
            /**
             * 🔥 CRITICAL:
             * Prevent ThreadLocal memory leak
             * (Tomcat reuses threads between requests)
             */
            requestContextService.clear();

            /**
             * Also clear Spring Security context
             * to avoid cross-request authentication leakage
             */
            SecurityContextHolder.clearContext();
        }
    }

    // =========================
    // Authenticated flow
    // =========================

    private void processAuthenticatedRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            String jwt) throws ServletException, IOException {

        try {
            // Extract claims safely
            String subject = jwtService.extractSubject(jwt)
                    .orElseThrow(() -> new TokenInvalidException("Missing subject"));

            String userName = jwtService.extractUserName(jwt)
                    .orElseThrow(() -> new TokenInvalidException("Missing username"));

            String application = jwtService.extractApplication(jwt)
                    .orElseThrow(() -> new TokenInvalidException("Missing application"));

            String tenant = jwtService.extractTenant(jwt)
                    .orElseThrow(() -> new TokenInvalidException("Missing tenant"));

            Boolean isAdmin = jwtService.extractIsAdmin(jwt);

            // Validate token (signature, expiration, etc.)
            if (!isTokenValid(jwt, tenant, application, userName)) {
                throw new TokenInvalidException("Token validation failed");
            }

            // Set Spring Security context
            setSecurityContext(subject, isAdmin);

            // Set request context (ThreadLocal + request attributes)
            requestContextService.setContextFromJwt(jwt, request);

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException | TokenInvalidException e) {
            log.warn("Authentication failed: {} {} → {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    e.getMessage());

            handleInvalidToken(response);
        }
    }

    /**
     * Populate Spring SecurityContext
     */
    private void setSecurityContext(String subject, Boolean isAdmin) {

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .username(subject)
                .isAdmin(Boolean.TRUE.equals(isAdmin))
                .password("password")
                .passwordExpired(false)
                .tenantEnabled(true)
                .accountEnabled(true)
                .accountExpired(false)
                .accountLocked(false)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new CustomAuthentification(
                        userDetails,
                        null,
                        Collections.emptyList()
                )
        );
    }

    // =========================
    // Error / fallback handling
    // =========================

    private void handleInvalidToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Invalid token\"}");
    }

    private void handleMissingToken(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (log.isDebugEnabled()) {
            log.debug("No JWT provided for {} {}", request.getMethod(), request.getRequestURI());
        }

        // Set minimal safe context
        requestContextService.setContext(DEFAULT_CONTEXT, request);

        filterChain.doFilter(request, response);
    }
}