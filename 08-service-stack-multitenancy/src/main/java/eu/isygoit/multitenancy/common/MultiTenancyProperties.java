package eu.isygoit.multitenancy.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The type Multi tenancy properties.
 */
@ConfigurationProperties(prefix = "multitenancy")
@Component
@Getter
@Setter
public class MultiTenancyProperties {

    private String mode;
    private List<TenantDataSourceConfig> tenants;
}
