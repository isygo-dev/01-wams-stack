package eu.isygoit.multitenancy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

@ConditionalOnProperty(
        name = "app.tenacy.request",
        havingValue = "true"
)
@Component
public class EntityManagerTenantFilterConfigurer {

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener
    public void applyFilter(ServletRequestHandledEvent event) {
        var session = entityManager.unwrap(Session.class);
        var filter = session.enableFilter("tenantFilter");
        filter.setParameter("tenantId", TenantContext.getTenantId());
    }
}
