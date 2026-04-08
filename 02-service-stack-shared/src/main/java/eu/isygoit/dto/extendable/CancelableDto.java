package eu.isygoit.dto.extendable;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * The type Abstract cancelable dto.
 *
 * @param <T> the type parameter
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class CancelableDto<T extends Serializable> extends IdAssignableDto<T> {

    private Boolean checkCancel = Boolean.FALSE;
    private Date cancelDate;
    private Long canceledBy;
}