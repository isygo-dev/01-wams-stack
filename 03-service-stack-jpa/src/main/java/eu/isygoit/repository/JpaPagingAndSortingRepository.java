package eu.isygoit.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.IIdAssignable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The interface Jpa paging and sorting repository.
 *
 * @param <T> the type parameter
 * @param <I> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface JpaPagingAndSortingRepository<T extends IIdAssignable<I>, I extends Serializable>
        extends JpaRepository<T, I>, JpaSpecificationExecutor<T> {

    /**
     * Find by id in list.
     *
     * @param ids the ids
     * @return the list
     */
    List<T> findByIdIn(List<I> ids);

    /**
     * Finds an entity by its ID only if it is not marked as canceled.
     * Use this method to automatically respect soft-delete logic.
     *
     * @param id the identifier
     * @return an optional containing the active entity, or empty if not found or canceled
     */
    default Optional<T> findActiveById(I id) {
        return findById(id).filter(entity -> {
            if (entity instanceof eu.isygoit.model.jakarta.CancelableEntity<?> cancelable) {
                return !Boolean.TRUE.equals(cancelable.getCheckCancel());
            }
            return true;
        });
    }
}
