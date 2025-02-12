package eu.isygoit.service;

import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.extendable.NextCodeModel;

/**
 * The interface Service.
 */
public interface IService {

    /**
     * Init code generator t.
     *
     * @param <E> the type parameter
     * @return the t
     */
    <E extends NextCodeModel> E initCodeGenerator();

    /**
     * Gets next code.
     *
     * @return the next code
     */
    String getNextCode();

    /**
     * Before persist e.
     *
     * @param <E>    the type parameter
     * @param entity the entity
     * @return the e
     */
    <E extends IIdEntity> E beforePersist(E entity);
}
