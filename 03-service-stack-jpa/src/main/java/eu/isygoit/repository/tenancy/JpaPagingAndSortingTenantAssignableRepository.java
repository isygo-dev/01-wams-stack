package eu.isygoit.repository.tenancy;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The interface Jpa paging and sorting tenant assignable repository.
 *
 * @param <T> the type parameter
 * @param <I> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface JpaPagingAndSortingTenantAssignableRepository<T extends ITenantAssignable & IIdAssignable<I>, I extends Serializable>
        extends JpaPagingAndSortingRepository<T, I> {

    /**
     * Find by tenant ignore case list.
     *
     * @param tenant the tenant
     * @return the list
     */
    List<T> findByTenantIgnoreCase(String tenant);

    /**
     * Find by tenant ignore case page.
     *
     * @param tenant   the tenant
     * @param pageable the pageable
     * @return the page
     */
    Page<T> findByTenantIgnoreCase(String tenant, Pageable pageable);


    /**
     * Find first by tenant ignore case optional.
     *
     * @param tenant the tenant
     * @return the optional
     */
    Optional<T> findFirstByTenantIgnoreCase(String tenant);

    /**
     * Find by tenant ignore case in list.
     *
     * @param tenant the tenant
     * @return the list
     */
    List<T> findByTenantIgnoreCaseIn(List<String> tenant);

    /**
     * Find by tenant ignore case in page.
     *
     * @param tenantList the tenant list
     * @param pageable   the pageable
     * @return the page
     */
    Page<T> findByTenantIgnoreCaseIn(List<String> tenantList, Pageable pageable);

    /**
     * Count by tenant ignore case long.
     *
     * @param tenant the tenant
     * @return the long
     */
    Long countByTenantIgnoreCase(String tenant);

    /**
     * Exists by id and tenant ignore case boolean.
     *
     * @param id     the id
     * @param tenant the tenant
     * @return the boolean
     */
    boolean existsByIdAndTenantIgnoreCase(I id, String tenant);
}
