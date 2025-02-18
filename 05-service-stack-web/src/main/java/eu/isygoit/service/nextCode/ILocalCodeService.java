package eu.isygoit.service.nextCode;

import eu.isygoit.model.extendable.NextCodeModel;

import java.util.Optional;

/**
 * The interface Local code service.
 *
 * @param <C> the type parameter
 */
public interface ILocalCodeService<C extends NextCodeModel> {

    /**
     * Find by entity optional.
     *
     * @param entity the entity
     * @return the optional
     */
    Optional<C> findByEntity(String entity);

    /**
     * Find by domain and entity and attribute optional.
     *
     * @param domain    the domain
     * @param entity    the entity
     * @param attribute the attribute
     * @return the optional
     */
    Optional<C> findByDomainAndEntityAndAttribute(String domain, String entity, String attribute);

    /**
     * Increment.
     *
     * @param domain    the domain
     * @param entity    the entity
     * @param increment the increment
     */
    void increment(String domain, String entity, Integer increment);

    /**
     * Save and flush c.
     *
     * @param nextCodeModel the next code model
     * @return the c
     */
    C saveAndFlush(C nextCodeModel);

    /**
     * Save c.
     *
     * @param nextCode the next code
     * @return the c
     */
    C save(C nextCode);
}
