package eu.isygoit.utils;

/**
 * The interface Tenant service.
 */
public interface ITenantService {

    /**
     * Initialize tenant model.
     *
     * @param tenantId the tenant id
     */
    void initializeTenantSchema(String tenantId);
}
