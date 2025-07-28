package eu.isygoit.service;

import eu.isygoit.common.ITenantValidator;
import eu.isygoit.constants.TenantConstants;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Default implementation of ITenantValidator.
 */
@Component
public class TenantValidator implements ITenantValidator {

    // You can later replace this with a dynamic source like DB or YAML config
    private final Set<String> validTenants = Set.of("tenants", "tenant1", "tenant2", "public", TenantConstants.SUPER_TENANT_NAME);

    @Override
    public boolean isValid(String tenantId) {
        return tenantId != null && validTenants.contains(tenantId);
    }
}