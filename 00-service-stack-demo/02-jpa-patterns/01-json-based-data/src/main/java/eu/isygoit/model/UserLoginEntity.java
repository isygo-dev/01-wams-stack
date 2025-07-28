package eu.isygoit.model;


import eu.isygoit.annotation.Criteria;
import eu.isygoit.annotation.JsonEntity;
import eu.isygoit.model.json.JsonElement;
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

    @Criteria
    private UUID id;
    @Criteria
    private String userId;
    @Criteria
    private String ip;
    @Criteria
    private String device;

    @Override
    public String getTenant() {
        return "";
    }

    @Override
    public void setTenant(String tenant) {

    }
}
