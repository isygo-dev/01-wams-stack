package eu.isygoit.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableDomain;
import eu.isygoit.model.AssignableId;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

/**
 * The interface Jpa paging and sorting saas codifiable repository.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface JpaPagingAndSortingAssignableDomainAndCodeRepository<I extends Serializable, E extends AssignableDomain & AssignableCode & AssignableId>
        extends JpaPagingAndSortingAssignableDomainRepository<I, E>, JpaPagingAndSortingAssignableCodeRepository<I, E> {

    /**
     * Find by domain ignore case and code ignore case optional.
     *
     * @param domain the domain
     * @param code   the code
     * @return the optional
     */
    Optional<E> findByDomainIgnoreCaseAndCodeIgnoreCase(String domain, String code);
}
