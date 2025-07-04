package eu.isygoit.multitenancy.common;


import eu.isygoit.model.ITenantAssignable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
//@JsonEntity(EventEntity.class)
public class UserLoginEntity implements JsonElement<UUID>, ITenantAssignable {

    private UUID Id;
    private String userId;
    private String ip;
    private String device;

    @Override
    public String getTenant() {
        return "";
    }

    @Override
    public void setTenant(String tenant) {

    }
}
