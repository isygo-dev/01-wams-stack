package eu.isygoit.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.AssignableDomain;
import eu.isygoit.model.extendable.NextCodeModel;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;
import java.util.Optional;

/**
 * The interface Next code repository.
 *
 * @param <I> the type parameter
 * @param <C> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface NextCodeRepository<C extends NextCodeModel & AssignableDomain, I extends Serializable>
        extends JpaPagingAndSortingAssignableDomainRepository<C, I> {

    /**
     * Find by entity optional.
     *
     * @param entity the entity
     * @return the optional
     */
    Optional<C> findByEntity(String entity);

    /**
     * Find by domain ignore case and entity and attribute optional.
     *
     * @param domain    the domain
     * @param entity    the entity
     * @param attribute the attribute
     * @return the optional
     */
    Optional<C> findByDomainIgnoreCaseAndEntityAndAttribute(String domain, String entity, String attribute);

    /**
     * Increment.
     *
     * @param domain    the domain
     * @param entity    the entity
     * @param increment the increment
     */
    @Modifying
    @Query("update AppNextCode set value = value + :increment where domain = :domain and entity = :entity")
    void increment(@Param("domain") String domain,
                   @Param("entity") String entity,
                   @Param("increment") Integer increment);
}
