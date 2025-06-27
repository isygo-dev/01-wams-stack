package eu.isygoit.multitenancy.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantDataSourceConfig {

    private String id;
    private String url;
    private String username;
    private String password;
}
