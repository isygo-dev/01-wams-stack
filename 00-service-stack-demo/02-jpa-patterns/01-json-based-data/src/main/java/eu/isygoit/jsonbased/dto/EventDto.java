package eu.isygoit.jsonbased.dto;

import com.fasterxml.jackson.databind.JsonNode;
import eu.isygoit.dto.extendable.AbstractAuditableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class EventDto extends AbstractAuditableDto<Long> {

    private String tenant;
    private JsonNode attributes;
}
