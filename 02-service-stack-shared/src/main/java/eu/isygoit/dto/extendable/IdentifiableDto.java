package eu.isygoit.dto.extendable;


import eu.isygoit.dto.IIdentifiableDto;
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
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class IdentifiableDto<T extends Serializable> extends AbstractDto
        implements IIdentifiableDto<T> {

    private T id;
}
