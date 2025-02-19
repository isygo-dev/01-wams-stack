package eu.isygoit.com.rest.service;

import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.model.AssignableId;
import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * The interface Crud service utils.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 */
public interface ICrudServiceUtils<E extends AssignableId, I extends Serializable> {

    /**
     * Gets repository.
     *
     * @return the repository
     * @throws JpaRepositoryNotDefinedException the jpa repository not defined exception
     */
    Repository getRepository() throws JpaRepositoryNotDefinedException;

    /**
     * Log and execute t.
     *
     * @param <T>        the type parameter
     * @param logMessage the log message
     * @param action     the action
     * @return the t
     */
    <T> T logAndExecute(String logMessage, Supplier<T> action);

    /**
     * Validate object.
     *
     * @param object     the object
     * @param mustHaveId the must have id
     */
    void validateObject(E object, boolean mustHaveId);

    /**
     * Validate object id.
     *
     * @param id the id
     */
    void validateObjectId(I id);

    /**
     * Check operation is allowed for domain.
     *
     * @param senderDomain the sender domain
     * @param id           the id
     * @param object       the object
     */
    void checkOperationIsAllowedForDomain(String senderDomain, I id, E object);

    /**
     * Process code assignable t.
     *
     * @param <T>    the type parameter
     * @param entity the entity
     * @return the t
     */
    <T extends AssignableId> T processCodeAssignable(T entity);

    /**
     * Process domain assignable t.
     *
     * @param <T>          the type parameter
     * @param senderDomain the sender domain
     * @param entity       the entity
     * @return the t
     */
    <T extends AssignableId> T processDomainAssignable(String senderDomain, T entity);
}
