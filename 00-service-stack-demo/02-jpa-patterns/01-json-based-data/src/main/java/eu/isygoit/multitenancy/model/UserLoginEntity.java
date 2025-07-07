package eu.isygoit.multitenancy.model;


import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.json.JsonElement;
import eu.isygoit.model.json.JsonEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonEntity(EventEntity.class)
public class UserLoginEntity implements JsonElement<UUID>, ITenantAssignable {

    private UUID id;
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
