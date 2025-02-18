package eu.isygoit.model.cassandra;

import eu.isygoit.model.AssignableId;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * The type Abstract entity.
 *
 * @param <E> the type parameter
 */
@Data
@SuperBuilder
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractEntity<I> implements AssignableId<I> {

}
