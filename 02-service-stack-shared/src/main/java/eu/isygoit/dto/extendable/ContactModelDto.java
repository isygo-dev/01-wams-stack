package eu.isygoit.dto.extendable;

import eu.isygoit.enums.IEnumContact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Contact model dto.
 *
 * @param <I> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class ContactModelDto<I extends Serializable> extends AbstractAuditableDto<I> {

    private IEnumContact.Types type;
    private String value;
}
