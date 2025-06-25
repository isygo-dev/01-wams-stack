package eu.isygoit.multitenancy.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties
public class MultiTenantProperties {

    @Value("${spring.jpa.properties.hibernate.multiTenancy}")
    private String multiTenancy;

    @Value("${spring.jpa.properties.hibernate.dialect}")
    private String dialect;

    @Value("${spring.jpa.show-sql}")
    private boolean showSql;

    @Value("${spring.jpa.format_sql}")
    private boolean formatSql;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
}
