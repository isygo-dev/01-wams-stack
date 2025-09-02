package eu.isygoit.kafka.dto;

import eu.isygoit.dto.extendable.AuditableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TutorialDto {

    private Long id;

    private String tenant;

    private String title;

    private String description;

    private boolean published;
}
