package eu.isygoit.multitenancy.common;

/**
 * The interface Tenant validator.
 */
public interface ITenantValidator {
    /**
     * Is valid boolean.
     *
     * @param tenantId the tenant id
     * @return the boolean
     */
    boolean isValid(String tenantId);
}
