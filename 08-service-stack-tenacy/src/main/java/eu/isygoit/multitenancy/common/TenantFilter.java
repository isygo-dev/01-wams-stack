package eu.isygoit.multitenancy.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * The type Tenant filter.
 */
@ConditionalOnProperty(name = "multi-tenancy.filter", havingValue = "TENANT")
@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    private final ITenantValidator tenantValidator;

    /**
     * Instantiates a new Tenant filter.
     *
     * @param tenantValidator the tenant validator
     */
    public TenantFilter(ITenantValidator tenantValidator) {
        this.tenantValidator = tenantValidator;
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
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid tenant: " + tenantId);
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