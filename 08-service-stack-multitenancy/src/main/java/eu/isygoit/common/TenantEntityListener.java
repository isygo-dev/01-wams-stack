package eu.isygoit.common;

import eu.isygoit.model.ITenantAssignable;
import jakarta.persistence.PrePersist;

/**
 * The type Tenant entity listener.
 */
public class TenantEntityListener {

    /**
     * Sets tenant.
     *
     * @param entity the entity
     */
    @PrePersist
    public void setTenant(Object entity) {
        if (entity instanceof ITenantAssignable tenantEntity && tenantEntity.getTenant() == null) {
            String tenantId = TenantContext.getTenantId(); // from ThreadLocal
            if (tenantId != null) {
                tenantEntity.setTenant(tenantId);
            } else {
                throw new IllegalStateException("Missing tenant ID in context");
            }
        }
    }
}
