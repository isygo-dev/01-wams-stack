package eu.isygoit.multitenancy;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(
        name = "app.tenacy.request",
        havingValue = "false"
)
@Component
public class HibernateTenantInterceptor {

    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    public void init() {
        ((Session) entityManager.getDelegate()).enableFilter("tenantFilter")
                .setParameter("tenantId", TenantContext.getTenantId());
    }
}
