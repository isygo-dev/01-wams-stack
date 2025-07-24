package eu.isygoit.dto.extendable;


import eu.isygoit.dto.IIdAssignableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Identifiable dto.
 *
 * @param <T> the type parameter
 */
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class IdAssignableDto<T extends Serializable> extends AbstractDto
        implements IIdAssignableDto<T> {

    private T id;
}
