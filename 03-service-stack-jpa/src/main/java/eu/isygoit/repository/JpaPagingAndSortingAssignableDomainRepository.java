package eu.isygoit.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.AssignableDomain;
import eu.isygoit.model.AssignableId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The interface Jpa paging and sorting saas repository.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface JpaPagingAndSortingAssignableDomainRepository<I extends Serializable, E extends AssignableDomain & AssignableId>
        extends JpaPagingAndSortingRepository<I, E> {


    /**
     * Find by domain ignore case list.
     *
     * @param domain the domain
     * @return the list
     */
    List<E> findByDomainIgnoreCase(String domain);

    /**
     * Find by domain ignore case page.
     *
     * @param domain   the domain
     * @param pageable the pageable
     * @return the page
     */
    Page<E> findByDomainIgnoreCase(String domain, Pageable pageable);

    /**
     * Find first by domain ignore case optional.
     *
     * @param domain the domain
     * @return the optional
     */
    Optional<E> findFirstByDomainIgnoreCase(String domain);

    /**
     * Find by domain ignore case in list.
     *
     * @param domain the domain
     * @return the list
     */
    List<E> findByDomainIgnoreCaseIn(List<String> domain);

    /**
     * Find by domain ignore case in page.
     *
     * @param domainList the domain list
     * @param pageable   the pageable
     * @return the page
     */
    Page<E> findByDomainIgnoreCaseIn(List<String> domainList, Pageable pageable);

    /**
     * Count by domain ignore case long.
     *
     * @param domain the domain
     * @return the long
     */
    Long countByDomainIgnoreCase(String domain);
}
