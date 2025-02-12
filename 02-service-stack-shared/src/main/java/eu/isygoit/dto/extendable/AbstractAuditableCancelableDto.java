package eu.isygoit.dto.extendable;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * The type Abstract auditable cancelable dto.
 *
 * @param <I> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractAuditableCancelableDto<I extends Serializable> extends AbstractCancelableDto<I> {

    private Date createDate;
    private String createdBy;
    private Date updateDate;
    private String updatedBy;
}
