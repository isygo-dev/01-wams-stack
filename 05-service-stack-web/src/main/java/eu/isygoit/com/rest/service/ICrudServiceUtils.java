package eu.isygoit.com.rest.service;

import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.model.IIdEntity;
import org.springframework.data.repository.Repository;

import java.io.Serializable;

/**
 * The interface Crud service utils.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 */
public interface ICrudServiceUtils<I extends Serializable, E extends IIdEntity> {

    /**
     * Repository repository.
     *
     * @return the repository
     * @throws JpaRepositoryNotDefinedException the jpa repository not defined exception
     */
    Repository repository() throws JpaRepositoryNotDefinedException;
}
