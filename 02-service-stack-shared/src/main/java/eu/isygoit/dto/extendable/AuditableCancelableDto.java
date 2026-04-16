package eu.isygoit.dto.extendable;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * The type Abstract auditable cancelable dto.
 *
 * @param <T> the type parameter
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class AuditableCancelableDto<T extends Serializable> extends CancelableDto<T> {

    private LocalDateTime createDate;
    private String createdBy;
    private LocalDateTime updateDate;
    private String updatedBy;
}
