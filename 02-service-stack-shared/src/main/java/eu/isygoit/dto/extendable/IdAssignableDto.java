package eu.isygoit.dto.extendable;


import eu.isygoit.dto.IIdAssignableDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Identifiable dto.
 *
 * @param <T> the type parameter
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class IdAssignableDto<T extends Serializable> extends AbstractDto<T> implements IIdAssignableDto<T> {

    private T id;
}
