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
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class ContactModelDto<T extends Serializable> extends AuditableDto<T> {

    private IEnumContact.Types type;
    private String value;
}
