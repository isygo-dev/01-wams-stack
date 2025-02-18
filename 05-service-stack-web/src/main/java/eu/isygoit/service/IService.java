package eu.isygoit.service;

import eu.isygoit.model.extendable.NextCodeModel;

import java.util.Optional;

/**
 * The interface Service.
 */
public interface IService {

    /**
     * Init code generator c.
     *
     * @param <C> the type parameter
     * @return the c
     */
    <C extends NextCodeModel> C initCodeGenerator();

    /**
     * Gets next code.
     *
     * @return the next code
     */
    Optional<String> getNextCode();
}
