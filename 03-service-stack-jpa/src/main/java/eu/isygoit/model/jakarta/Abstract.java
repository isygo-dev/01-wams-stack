package eu.isygoit.model.jakarta;

import eu.isygoit.model.AssignableId;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Abstract entity.
 *
 * @param <I> the type parameter
 */
@Data
@SuperBuilder
@AllArgsConstructor
@MappedSuperclass
public abstract class Abstract<I extends Serializable> implements AssignableId<I> {

}
