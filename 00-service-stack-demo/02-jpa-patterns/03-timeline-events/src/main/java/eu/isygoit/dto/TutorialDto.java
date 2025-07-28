package eu.isygoit.dto;

import eu.isygoit.dto.extendable.AbstractAuditableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Tutorial dto.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TutorialDto extends AbstractAuditableDto<Long> implements ITenantAssignableDto {

    private Long id;

    private String tenant;

    private String title;

    private String description;

    private boolean published;
}
