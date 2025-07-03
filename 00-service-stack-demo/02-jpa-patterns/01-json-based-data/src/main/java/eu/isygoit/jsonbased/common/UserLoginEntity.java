package eu.isygoit.jsonbased.common;


import eu.isygoit.jsonbased.model.EventEntity;
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
public class UserLoginEntity implements JsonElement<UUID> {

    private UUID Id;
    private String userId;
    private String ip;
    private String device;
}
