package eu.isygoit.multitenancy.common;

import eu.isygoit.constants.TenantConstants;
import org.springframework.util.StringUtils;

public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static String getTenantId() {
        return CURRENT_TENANT.get() != null
                ? CURRENT_TENANT.get()
                : TenantConstants.DEFAULT_TENANT_NAME;
    }

    public static void setTenantId(String tenant) {
        if (StringUtils.hasText(tenant)) {
            CURRENT_TENANT.set(tenant);
        } else {
            CURRENT_TENANT.set(TenantConstants.DEFAULT_TENANT_NAME);
        }
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
