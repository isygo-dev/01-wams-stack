package eu.isygoit.common;

import eu.isygoit.audit.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Tenant filter.
 */
@ConditionalOnExpression(
        "'${app.tenancy.enabled}'=='true' && '${app.tenancy.filter}'=='TENANT'"
)
@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final String SWAGGER_PATTERN = "/swagger-ui";
    private static final String API_DOCS_PATTERN = "/v3/api-docs";
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final Map<String, Boolean> URI_FILTER_CACHE = new ConcurrentHashMap<>();

    private final ITenantValidator tenantValidator;

    /**
     * Instantiates a new Tenant filter.
     *
     * @param tenantValidator the tenant validator
     */
    public TenantFilter(ITenantValidator tenantValidator) {
        this.tenantValidator = tenantValidator;
    }

    private boolean shouldSkipUri(String uri) {
        return uri.contains(SWAGGER_PATTERN) ||
                uri.contains(API_DOCS_PATTERN);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId = request.getHeader(TENANT_HEADER);

        // Check if header is missing
        if (tenantId == null || tenantId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required header: " + TENANT_HEADER);
            return;
        }

        // Check if tenant is valid
        if (!tenantValidator.isValid(tenantId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant: " + tenantId);
            return;
        }

        try {
            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}