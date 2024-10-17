package eu.isygoit.com.rest.service;

import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.model.IIdEntity;
import org.springframework.data.repository.Repository;

/**
 * The interface Crud service utils.
 *
 * @param <T> the type parameter
 */
public interface ICrudServiceUtils<T extends IIdEntity> {

    /**
     * Repository repository.
     *
     * @return the repository
     * @throws JpaRepositoryNotDefinedException the jpa repository not defined exception
     */
    Repository repository() throws JpaRepositoryNotDefinedException;
}