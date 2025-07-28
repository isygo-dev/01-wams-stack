package eu.isygoit.repository.message;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.extendable.LocaleMessageModel;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

/**
 * The interface Message model repository.
 *
 * @param <T> the type parameter
 * @param <I> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface MessageModelRepository<T extends LocaleMessageModel<I> & IIdAssignable<I>, I extends Serializable>
        extends JpaPagingAndSortingRepository<T, I> {

    /**
     * Find by code ignore case and locale optional.
     *
     * @param code   the code
     * @param locale the locale
     * @return the optional
     */
    Optional<LocaleMessageModel> findByCodeIgnoreCaseAndLocale(String code, String locale);
}
