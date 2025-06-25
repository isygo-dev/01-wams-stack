package eu.isygoit.multitenancy.discriminator;

import eu.isygoit.multitenancy.common.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.multiTenancy", havingValue = "DISCRIMINATOR")
@Component
public class TenantFilterRequestConfigurer {

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener
    public void applyFilter(ServletRequestHandledEvent event) {
        var session = entityManager.unwrap(Session.class);
        var filter = session.enableFilter("tenantFilter");
        filter.setParameter("tenantId", TenantContext.getTenantId());
    }
}
