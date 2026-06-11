package eu.isygoit.dto.extendable;


import eu.isygoit.enums.IEnumAccountOrigin;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Account model dto.
 *
 * @param <T> the type parameter
 */

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class AccountModelDto<T extends Serializable> extends AuditableIdAssignableDto<T> {

    private String code;
    @NotEmpty
    private String email;
    private String fullName;
    @Builder.Default
    private String origin = IEnumAccountOrigin.Types.SYS_ADMIN.name();
}
