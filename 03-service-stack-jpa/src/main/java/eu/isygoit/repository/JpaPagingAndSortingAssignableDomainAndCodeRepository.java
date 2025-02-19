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
public interface JpaPagingAndSortingAssignableDomainAndCodeRepository<E extends AssignableDomain & AssignableCode & AssignableId, I extends Serializable>
        extends JpaPagingAndSortingAssignableDomainRepository<E, I>, JpaPagingAndSortingAssignableCodeRepository<E, I> {

    /**
     * Find by domain ignore case and code ignore case optional.
     *
     * @param domain the domain
     * @param code   the code
     * @return the optional
     */
    Optional<E> findByDomainIgnoreCaseAndCodeIgnoreCase(String domain, String code);
}
