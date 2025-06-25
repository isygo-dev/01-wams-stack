package eu.isygoit.multitenancy.common;

import eu.isygoit.model.ITenantAssignable;
import jakarta.persistence.PrePersist;

public class TenantEntityListener {

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
