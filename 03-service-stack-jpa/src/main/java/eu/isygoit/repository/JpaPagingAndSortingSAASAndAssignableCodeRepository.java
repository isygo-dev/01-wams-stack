package eu.isygoit.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.IAssignableCode;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ISAASEntity;
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
public interface JpaPagingAndSortingSAASAndAssignableCodeRepository<I extends Serializable, E extends ISAASEntity & IAssignableCode & IIdEntity>
        extends JpaPagingAndSortingSAASRepository<I, E>, JpaPagingAndSortingAndAssignableCodeRepository<I, E> {

    /**
     * Find by domain ignore case and code ignore case optional.
     *
     * @param domain the domain
     * @param code   the code
     * @return the optional
     */
    Optional<E> findByDomainIgnoreCaseAndCodeIgnoreCase(String domain, String code);
}
