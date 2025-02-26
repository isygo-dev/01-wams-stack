package eu.isygoit.service.nextCode;

import eu.isygoit.model.extendable.NextCodeModel;

import java.util.Optional;

/**
 * The interface Next code service.
 *
 * @param <T> the type parameter
 */
public interface ICodeGeneratorService<T extends NextCodeModel> {

    /**
     * Find by entity t.
     *
     * @param entity the entity
     * @return the t
     */
    Optional<T> findByEntity(String entity);

    /**
     * Find by domain and entity and attribute t.
     *
     * @param domain    the domain
     * @param entity    the entity
     * @param attribute the attribute
     * @return the t
     */
    Optional<T> findByDomainAndEntityAndAttribute(String domain, String entity, String attribute);

    /**
     * Increment.
     *
     * @param domain    the domain
     * @param entity    the entity
     * @param increment the increment
     */
    void increment(String domain, String entity, Integer increment);

    /**
     * Save and flush t.
     *
     * @param nextCodeModel the next code model
     * @return the t
     */
    T saveAndFlush(T nextCodeModel);

    /**
     * Save t.
     *
     * @param nextCode the next code
     * @return the t
     */
    T save(T nextCode);
}
