package eu.isygoit.common;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * The type Tenant identifier resolver.
 */
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getTenantId();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

