package eu.isygoit.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IDomainAssignable;
import eu.isygoit.model.IIdAssignable;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

/**
 * The interface Jpa paging and sorting saas codeAssignable repository.
 *
 * @param <T> the type parameter
 * @param <I> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface JpaPagingAndSortingDomainAndCodeAssignableRepository<T extends IDomainAssignable & ICodeAssignable & IIdAssignable<I>, I extends Serializable>
        extends JpaPagingAndSortingDomainAssignableRepository<T, I>, JpaPagingAndSortingCodeAssingnableRepository<T, I> {

    /**
     * Find by domain ignore case and code ignore case optional.
     *
     * @param domain the domain
     * @param code   the code
     * @return the optional
     */
    Optional<T> findByDomainIgnoreCaseAndCodeIgnoreCase(String domain, String code);
}
