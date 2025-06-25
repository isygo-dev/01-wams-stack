package eu.isygoit.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.ITenantAssignable;
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
 * @param <T> the type parameter
 * @param <I> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface NextCodeRepository<T extends NextCodeModel<I> & ITenantAssignable, I extends Serializable>
        extends JpaPagingAndSortingTenantAssignableRepository<T, I> {

    /**
     * Find by entity optional.
     *
     * @param entity the entity
     * @return the optional
     */
    Optional<T> findByEntity(String entity);

    /**
     * Find by tenant ignore case and entity and attribute optional.
     *
     * @param tenant    the tenant
     * @param entity    the entity
     * @param attribute the attribute
     * @return the optional
     */
    Optional<T> findByTenantIgnoreCaseAndEntityAndAttribute(String tenant, String entity, String attribute);

    /**
     * Increment.
     *
     * @param tenant    the tenant
     * @param entity    the entity
     * @param increment the increment
     */
    @Modifying
    @Query("update AppNextCode set value = value + :increment where tenant = :tenant and entity = :entity")
    void increment(@Param("tenant") String tenant,
                   @Param("entity") String entity,
                   @Param("increment") Integer increment);
}
