package eu.isygoit.dto;

import com.fasterxml.jackson.databind.JsonNode;
import eu.isygoit.dto.extendable.AuditableDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class EventDto extends AuditableDto<Long> {

    @Setter
    private Long id;
    private String tenant;
    private JsonNode attributes;
}
