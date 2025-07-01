package eu.isygoit.multitenancy.common;

import eu.isygoit.constants.JwtConstants;
import eu.isygoit.dto.common.RequestContextDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * The type Tenant to context filter.
 */
@ConditionalOnProperty(name = "multi-tenancy.filter", havingValue = "CONTEXT")
@Component
public class TenantToContextFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    private final ITenantValidator tenantValidator;

    /**
     * Instantiates a new Tenant to context filter.
     *
     * @param tenantValidator the tenant validator
     */
    public TenantToContextFilter(ITenantValidator tenantValidator) {
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

            // Add context attributes to request
            RequestContextDto contextDto = buildRequestContext(tenantId, null, null, null);
            addAttributes(request, Map.of(JwtConstants.JWT_USER_CONTEXT, contextDto));

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Builds a RequestContextDto from token claims.
     */
    private RequestContextDto buildRequestContext(String tenant, String userName, Boolean isAdmin, String application) {
        return RequestContextDto.builder()
                .senderTenant(tenant)
                .senderUser(userName)
                .isAdmin(isAdmin)
                .logApp(application)
                .build();
    }

    /**
     * Adds attributes to the request.
     * Efficiently adds all provided attributes to the request attributes.
     *
     * @param request    the HTTP request to add attributes to
     * @param attributes map of attributes to add
     */
    public void addAttributes(HttpServletRequest request, Map<String, Object> attributes) {
        if (!CollectionUtils.isEmpty(attributes)) {
            // Use direct method reference for better performance
            attributes.forEach(request::setAttribute);
        }
    }
}