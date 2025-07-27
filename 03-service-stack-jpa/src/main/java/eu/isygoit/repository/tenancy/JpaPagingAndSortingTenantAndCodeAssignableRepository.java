package eu.isygoit.repository.tenancy;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.repository.JpaPagingAndSortingCodeAssingnableRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

/**
 * The interface Jpa paging and sorting tenant and code assignable repository.
 *
 * @param <T> the type parameter
 * @param <I> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface JpaPagingAndSortingTenantAndCodeAssignableRepository<T extends ITenantAssignable & ICodeAssignable & IIdAssignable<I>, I extends Serializable>
        extends JpaPagingAndSortingTenantAssignableRepository<T, I>, JpaPagingAndSortingCodeAssingnableRepository<T, I> {

    /**
     * Find by tenant ignore case and code ignore case optional.
     *
     * @param tenant the tenant
     * @param code   the code
     * @return the optional
     */
    Optional<T> findByTenantIgnoreCaseAndCodeIgnoreCase(String tenant, String code);
}
