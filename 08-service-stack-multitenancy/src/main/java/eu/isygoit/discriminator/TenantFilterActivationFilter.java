package eu.isygoit.discriminator;

import eu.isygoit.common.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * The type Tenant filter activation filter.
 */
@Component
@ConditionalOnExpression(
        "'${app.tenancy.enabled}'=='true' && '${app.tenancy.mode}'=='DISCRIMINATOR'"
)
public class TenantFilterActivationFilter extends OncePerRequestFilter {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Get current tenant from context
        String tenantId = TenantContext.getTenantId();

        // Activate Hibernate filter
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("tenantFilter");
        filter.setParameter("tenantId", tenantId);

        filterChain.doFilter(request, response);
    }
}
