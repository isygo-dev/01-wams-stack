package eu.isygoit.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ISAASEntity;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

/**
 * The interface Jpa paging and sorting sas codifiable repository.
 *
 * @param <T> the type parameter
 * @param <I> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface JpaPagingAndSortingSAASCodifiableRepository<T extends ISAASEntity & ICodifiable & IIdEntity, I extends Serializable>
        extends JpaPagingAndSortingSAASRepository<T, I>, JpaPagingAndSortingCodifiableRepository<T, I> {

    /**
     * Find by domain ignore case and code ignore case optional.
     *
     * @param domain the domain
     * @param code   the code
     * @return the optional
     */
    Optional<T> findByDomainIgnoreCaseAndCodeIgnoreCase(String domain, String code);
}
