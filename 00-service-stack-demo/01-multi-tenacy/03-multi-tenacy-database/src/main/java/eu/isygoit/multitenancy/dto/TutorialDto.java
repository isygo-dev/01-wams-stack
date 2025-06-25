package eu.isygoit.multitenancy.dto;

import eu.isygoit.dto.extendable.AbstractAuditableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TutorialDto extends AbstractAuditableDto<Long> {

    private Long id;

    private String tenant;

    private String title;

    private String description;

    private boolean published;
}
