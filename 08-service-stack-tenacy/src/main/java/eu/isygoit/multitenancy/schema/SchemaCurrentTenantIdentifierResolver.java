package eu.isygoit.multitenancy.schema;

import eu.isygoit.multitenancy.common.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class SchemaCurrentTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {
    private static final String DEFAULT_TENANT = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        return (tenantId != null) ? tenantId : DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
