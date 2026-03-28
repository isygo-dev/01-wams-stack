package eu.isygoit.dto;

import eu.isygoit.dto.extendable.AuditableDto;
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
public class TutorialDto extends AuditableDto<Long> implements ITenantAssignableDto {

    private Long id;

    private String tenant;

    private String title;

    private String description;

    private boolean published;
}
