package eu.isygoit.common;

import lombok.Data;

/**
 * The type Tenant data source config.
 */
@Data
public class TenantDataSourceConfig {

    private String id;
    private String url;
    private String username;
    private String password;
}
