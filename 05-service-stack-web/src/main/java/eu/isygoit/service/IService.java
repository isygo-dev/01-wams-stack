package eu.isygoit.service;

import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.extendable.NextCodeModel;

import java.util.Optional;

/**
 * The interface Service.
 */
public interface IService {

    /**
     * Init code generator optional.
     *
     * @param <T> the type parameter
     * @return the optional
     */
    <T extends NextCodeModel> Optional<T> initializeCodeGenerator();

    /**
     * Gets next code.
     *
     * @return the next code
     */
    Optional<String> generateNextCode();

    /**
     * Before persist e.
     *
     * @param <E>    the type parameter
     * @param entity the entity
     * @return the e
     */
    <E extends IIdEntity> E beforeSave(E entity);
}
