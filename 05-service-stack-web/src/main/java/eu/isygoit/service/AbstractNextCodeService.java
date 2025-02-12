package eu.isygoit.service;

import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.repository.NextCodeRepository;
import eu.isygoit.service.nextCode.INextCodeService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * The type Abstract next code service.
 *
 * @param <E> the type parameter
 */
public abstract class AbstractNextCodeService<E extends NextCodeModel> implements INextCodeService<E> {

    /**
     * Next code repository next code repository.
     *
     * @return the next code repository
     */
    public abstract NextCodeRepository nextCodeRepository();

    @Override
    public E findByEntity(String entity) {
        Optional<E> nextCode = nextCodeRepository().findByEntity(entity);
        if (nextCode.isPresent()) {
            return nextCode.get();
        }
        return null;
    }

    @Override
    public E findByDomainAndEntityAndAttribute(String domain, String entity, String attribute) {
        Optional<E> nextCode = nextCodeRepository().findByDomainIgnoreCaseAndEntityAndAttribute(domain, entity, attribute);
        if (nextCode.isPresent()) {
            return nextCode.get();
        }
        return null;
    }

    @Override
    @Transactional
    public void increment(String domain, String entity, Integer increment) {
        nextCodeRepository().increment(domain, entity, increment);
        nextCodeRepository().flush();
    }

    public E saveAndFlush(E appNextCode) {
        return (E) nextCodeRepository().saveAndFlush(appNextCode);
    }

    public E save(E appNextCode) {
        return (E) nextCodeRepository().save(appNextCode);
    }
}
