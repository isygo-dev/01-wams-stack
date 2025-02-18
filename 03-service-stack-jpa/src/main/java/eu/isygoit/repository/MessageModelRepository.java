package eu.isygoit.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.AssignableId;
import eu.isygoit.model.extendable.LocaleMessageModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

/**
 * The interface Message model repository.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface MessageModelRepository<I extends Serializable, E extends AssignableId>
        extends JpaRepository<E, I> {

    /**
     * Find by code ignore case and locale optional.
     *
     * @param code   the code
     * @param locale the locale
     * @return the optional
     */
    Optional<LocaleMessageModel> findByCodeIgnoreCaseAndLocale(String code, String locale);
}
