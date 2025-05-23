package eu.isygoit.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.IDomainAssignable;
import eu.isygoit.model.IIdAssignable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The interface Jpa paging and sorting saas repository.
 *
 * @param <T> the type parameter
 * @param <I> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface JpaPagingAndSortingSAASRepository<T extends IDomainAssignable & IIdAssignable<I>, I extends Serializable>
        extends JpaPagingAndSortingRepository<T, I> {


    /**
     * Find by domain ignore case list.
     *
     * @param domain the domain
     * @return the list
     */
    List<T> findByDomainIgnoreCase(String domain);

    /**
     * Find by domain ignore case page.
     *
     * @param domain   the domain
     * @param pageable the pageable
     * @return the page
     */
    Page<T> findByDomainIgnoreCase(String domain, Pageable pageable);

    /**
     * Find first by domain ignore case optional.
     *
     * @param domain the domain
     * @return the optional
     */
    Optional<T> findFirstByDomainIgnoreCase(String domain);

    /**
     * Find by domain ignore case in list.
     *
     * @param domain the domain
     * @return the list
     */
    List<T> findByDomainIgnoreCaseIn(List<String> domain);

    /**
     * Find by domain ignore case in page.
     *
     * @param domainList the domain list
     * @param pageable   the pageable
     * @return the page
     */
    Page<T> findByDomainIgnoreCaseIn(List<String> domainList, Pageable pageable);

    /**
     * Count by domain ignore case long.
     *
     * @param domain the domain
     * @return the long
     */
    Long countByDomainIgnoreCase(String domain);
}
