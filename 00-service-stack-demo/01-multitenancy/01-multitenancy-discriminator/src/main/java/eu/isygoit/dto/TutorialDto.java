package eu.isygoit.dto;

import eu.isygoit.dto.extendable.AuditableIdAssignableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TutorialDto extends AuditableIdAssignableDto<Long> {

    private Long id;

    private String tenant;

    private String title;

    private String description;

    private boolean published;
}
