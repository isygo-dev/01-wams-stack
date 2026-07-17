package eu.isygoit.com.rest.service.tenancy;

import eu.isygoit.constants.TenantConstants;
import eu.isygoit.exception.OperationNotAllowedException;
import eu.isygoit.model.ITenantAssignable;
import org.springframework.util.StringUtils;

public class TenantHelper {

    public static <T extends ITenantAssignable> T assignTenantIfApplicable(String tenant, T entity) {
        if (!StringUtils.hasText(tenant) || entity == null) {
            throw new OperationNotAllowedException("Tenant or entity are null or empty");
        }

        if (TenantConstants.SUPER_TENANT_NAME.equals(tenant)) {
            if (!StringUtils.hasText(entity.getTenant())) {
                entity.setTenant(TenantConstants.SUPER_TENANT_NAME);
            }
        } else {
            entity.setTenant(tenant);
        }

        return entity;
    }
}
