package eu.isygoit.jsonbased.common;

import eu.isygoit.jsonbased.model.EventEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
//@JsonEntity(EventEntity.class)
public class OrderPlacedEntity implements JsonElement<UUID> {

    private UUID Id;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
}
