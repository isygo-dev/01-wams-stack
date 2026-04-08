package eu.isygoit.model.jakarta;

import eu.isygoit.model.IIdAssignable;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@SuperBuilder
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractEntity<T extends Serializable> implements IIdAssignable<T> {

}