package eu.isygoit.model.jakarta;

import eu.isygoit.model.IIdAssignable;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractEntity<T extends Serializable> implements IIdAssignable<T> {

}