package eu.isygoit.dto;


/**
 * The interface Isas dto.
 */
public interface ITenantAssignableDto extends IDto {

    /**
     * Gets tenant.
     *
     * @return the tenant
     */
    String getTenant();

    /**
     * Sets tenant.
     *
     * @param tenant the tenant
     */
    void setTenant(String tenant);
}
