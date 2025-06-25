package eu.isygoit.dto.common;

import eu.isygoit.dto.extendable.AbstractAuditableDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type User context dto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserContextDto extends AbstractAuditableDto<Long> {

    @NotEmpty
    private String tenant;
    @NotEmpty
    private String application;
    @NotEmpty
    private String userName;
    @NotEmpty
    private String fullName;
}
