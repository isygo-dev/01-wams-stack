package eu.isygoit.dto.extendable;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * The type Abstract cancelable dto.
 *
 * @param <I> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractCancelableDto<I extends Serializable> extends IdentifiableDto<I> {

    private Boolean checkCancel = Boolean.FALSE;
    private Date cancelDate;
    private Long canceledBy;
}