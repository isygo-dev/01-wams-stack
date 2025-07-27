package eu.isygoit.multitenancy.utils;

/**
 * The interface Tenant service.
 */
public interface ITenantService {

    /**
     * Initialize tenant schema.
     *
     * @param tenantId the tenant id
     */
    void initializeTenantSchema(String tenantId);
}
