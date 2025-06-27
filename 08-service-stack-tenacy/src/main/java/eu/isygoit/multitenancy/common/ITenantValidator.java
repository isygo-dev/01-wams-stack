package eu.isygoit.multitenancy.common;

public interface ITenantValidator {
    boolean isValid(String tenantId);
}
