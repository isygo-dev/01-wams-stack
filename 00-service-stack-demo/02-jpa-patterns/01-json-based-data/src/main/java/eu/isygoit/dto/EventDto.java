package eu.isygoit.dto;

import com.fasterxml.jackson.databind.JsonNode;
import eu.isygoit.dto.extendable.AuditableIdAssignableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class EventDto extends AuditableIdAssignableDto<Long> {


    private Long id;
    private String tenant;
    private JsonNode attributes;
}
