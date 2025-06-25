package eu.isygoit.multitenancy.database;

import eu.isygoit.multitenancy.common.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;

public class MultiTenantRoutingDataSource extends AbstractRoutingDataSource {

    @Autowired
    private DatabasePerTenantConnectionProvider tenantProvider;

    public MultiTenantRoutingDataSource() {
        // Obligation de définir une map même vide
        super.setTargetDataSources(new HashMap<>());
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getTenantId();
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String tenantId = (String) determineCurrentLookupKey();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant ID not set in TenantContext");
        }
        return tenantProvider.getDataSource(tenantId);
    }
}
