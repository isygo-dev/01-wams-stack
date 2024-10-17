package eu.isygoit.model.cassandra;

import eu.isygoit.model.IIdEntity;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * The type Abstract entity.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractEntity<T> implements IIdEntity<T> {

}
