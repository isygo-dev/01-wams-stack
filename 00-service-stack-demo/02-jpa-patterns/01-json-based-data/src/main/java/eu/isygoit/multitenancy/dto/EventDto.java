package eu.isygoit.multitenancy.dto;

import com.fasterxml.jackson.databind.JsonNode;
import eu.isygoit.dto.extendable.AbstractAuditableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class EventDto extends AbstractAuditableDto<Long> {

    private Long id;
    private String tenant;
    private JsonNode attributes;
}
