package eu.isygoit.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableId;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The interface Jpa paging and sorting codifiable repository.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface JpaPagingAndSortingAssignableCodeRepository<I extends Serializable, E extends AssignableCode & AssignableId>
        extends JpaPagingAndSortingRepository<I, E> {

    /**
     * Exists by code ignore case boolean.
     *
     * @param code the code
     * @return the boolean
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Find by code ignore case optional.
     *
     * @param code the code
     * @return the optional
     */
    Optional<E> findByCodeIgnoreCase(String code);

    /**
     * Find by code ignore case in list.
     *
     * @param codeList the code list
     * @return the list
     */
    List<E> findByCodeIgnoreCaseIn(List<String> codeList);
}
