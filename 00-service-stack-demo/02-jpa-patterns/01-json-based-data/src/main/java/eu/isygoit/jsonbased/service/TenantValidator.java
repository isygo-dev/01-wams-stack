package eu.isygoit.jsonbased.service;

import eu.isygoit.constants.TenantConstants;
import eu.isygoit.multitenancy.common.ITenantValidator;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Default implementation of ITenantValidator.
 */
@Component
public class TenantValidator implements ITenantValidator {

    // You can later replace this with a dynamic source like DB or YAML config
    private final Set<String> validTenants = Set.of("tenant1", "tenant2", "public", TenantConstants.SUPER_TENANT_NAME);

    @Override
    public boolean isValid(String tenantId) {
        return tenantId != null && validTenants.contains(tenantId);
    }
}