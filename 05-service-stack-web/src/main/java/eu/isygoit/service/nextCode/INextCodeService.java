package eu.isygoit.service.nextCode;

import eu.isygoit.model.extendable.NextCodeModel;

/**
 * The interface Next code service.
 *
 * @param <E> the type parameter
 */
public interface INextCodeService<E extends NextCodeModel> {

    /**
     * Find by entity t.
     *
     * @param entity the entity
     * @return the t
     */
    E findByEntity(String entity);

    /**
     * Find by domain and entity and attribute t.
     *
     * @param domain    the domain
     * @param entity    the entity
     * @param attribute the attribute
     * @return the t
     */
    E findByDomainAndEntityAndAttribute(String domain, String entity, String attribute);

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
    E saveAndFlush(E nextCodeModel);

    /**
     * Save t.
     *
     * @param nextCode the next code
     * @return the t
     */
    E save(E nextCode);
}
