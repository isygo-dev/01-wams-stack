package eu.isygoit.multitenancy.dto;

import eu.isygoit.dto.extendable.IdAssignableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEventDto extends IdAssignableDto<UUID> {

    private String orderId;
    private String customerId;
    private BigDecimal amount;
}
