package eu.isygoit.dto;

import eu.isygoit.dto.extendable.AuditableIdAssignableDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * The type Tutorial dto.
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TutorialDto extends AuditableIdAssignableDto<Long> implements ITenantAssignableDto {

    private Long id;

    private String tenant;

    private String title;

    private String description;

    private boolean published;
}
