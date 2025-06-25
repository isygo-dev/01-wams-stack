package eu.isygoit.multitenancy.common;

import eu.isygoit.constants.TenantConstants;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        return (tenantId != null) ? tenantId : TenantConstants.DEFAULT_TENANT_NAME;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

