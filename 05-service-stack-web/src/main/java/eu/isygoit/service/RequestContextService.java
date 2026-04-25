package eu.isygoit.service;

import eu.isygoit.dto.common.ContextRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestContextService {

    /**
     * ThreadLocal used to store request context per thread.
     * IMPORTANT: Must always be cleared to avoid memory leaks in thread pools.
     */
    private static final ThreadLocal<ContextRequestDto> CURRENT_CONTEXT = new ThreadLocal<>();

    private final ITokenService jwtService;

    /**
     * Populate context from DTO and store it:
     * - In HttpServletRequest attributes (for request scope access)
     * - In ThreadLocal (for non-web layers access)
     */
    public void setContext(ContextRequestDto context, HttpServletRequest request) {
        if (context == null || request == null) {
            log.warn("Attempt to set null context or request");
            return;
        }

        // Store in request attributes (preferred in web scope)
        setIfNotNull(request, ContextRequestDto.x_sender_tenant, context.getSenderTenant());
        setIfNotNull(request, ContextRequestDto.x_sender_user, context.getSenderUser());
        setIfNotNull(request, ContextRequestDto.x_log_app, context.getLogApp());
        setIfNotNull(request, ContextRequestDto.x_is_admin, context.getIsAdmin());

        // Store in ThreadLocal (fallback for non-web usage)
        CURRENT_CONTEXT.set(context);
    }

    /**
     * Build context from JWT and delegate to main setter.
     * Throws explicit exception instead of generic NoSuchElementException.
     */
    public void setContextFromJwt(String jwt, HttpServletRequest request) {
        if (jwt == null || jwt.isBlank()) {
            throw new IllegalArgumentException("JWT must not be null or empty");
        }

        ContextRequestDto context = ContextRequestDto.builder()
                .senderUser(jwtService.extractUserName(jwt)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid JWT: missing username")))
                .logApp(jwtService.extractApplication(jwt)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid JWT: missing application")))
                .senderTenant(jwtService.extractTenant(jwt)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid JWT: missing tenant")))
                .isAdmin(jwtService.extractIsAdmin(jwt))
                .build();

        setContext(context, request);
    }

    /**
     * Retrieve current context:
     * Priority:
     * 1. ThreadLocal (fast access)
     * 2. HttpServletRequest attributes
     * 3. Http headers (LOW trust → should be validated)
     */
    public ContextRequestDto getCurrentContext() {

        // 1. ThreadLocal (fastest)
        ContextRequestDto context = CURRENT_CONTEXT.get();
        if (context != null) {
            return context;
        }

        // 2. Request attributes
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            return ContextRequestDto.builder().build();
        }

        HttpServletRequest request = attrs.getRequest();

        String tenant = getAttributeOrHeader(request, ContextRequestDto.x_sender_tenant);
        String user = getAttributeOrHeader(request, ContextRequestDto.x_sender_user);
        String app = getAttributeOrHeader(request, ContextRequestDto.x_log_app);

        Boolean isAdmin = getBooleanAttributeOrHeader(request, ContextRequestDto.x_is_admin);

        return ContextRequestDto.builder()
                .senderTenant(tenant)
                .senderUser(user)
                .logApp(app)
                .isAdmin(isAdmin)
                .build();
    }

    /**
     * MUST be called at end of request (e.g., in Filter or Interceptor)
     * Prevents ThreadLocal memory leaks in container thread pools.
     */
    public void clear() {
        CURRENT_CONTEXT.remove();
    }

    // =======================
    // Utility methods
    // =======================

    private void setIfNotNull(HttpServletRequest request, String key, Object value) {
        if (value != null) {
            request.setAttribute(key, value);
        }
    }

    /**
     * Retrieves value from request attribute first, then header.
     * WARNING: Headers can be spoofed → should not be trusted blindly.
     */
    private String getAttributeOrHeader(HttpServletRequest request, String key) {
        Object attr = request.getAttribute(key);
        if (attr instanceof String str) {
            return str;
        }
        return request.getHeader(key);
    }

    /**
     * Safe boolean parsing from attribute or header.
     */
    private Boolean getBooleanAttributeOrHeader(HttpServletRequest request, String key) {
        Object attr = request.getAttribute(key);
        if (attr instanceof Boolean bool) {
            return bool;
        }

        String header = request.getHeader(key);
        return header != null ? Boolean.parseBoolean(header) : null;
    }
}