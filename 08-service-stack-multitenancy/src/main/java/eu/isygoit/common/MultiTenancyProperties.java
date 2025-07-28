package eu.isygoit.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The type Multi json properties.
 */
@ConditionalOnProperty(name = "app.tenancy.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "app.tenancy")
@Component
@Getter
@Setter
public class MultiTenancyProperties {

    private String mode;
    private List<TenantDataSourceConfig> tenants;
}
