package eu.isygoit.model.jakarta;

import eu.isygoit.model.IIdEntity;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * The type Abstract entity.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractEntity<T extends Serializable> implements IIdEntity<T> {

}
