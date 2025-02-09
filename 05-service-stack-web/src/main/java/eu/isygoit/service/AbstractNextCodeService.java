package eu.isygoit.service;

import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.repository.NextCodeRepository;
import eu.isygoit.service.nextCode.INextCodeService;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Optional;

/**
 * The type Abstract next code service.
 *
 * @param <T> the type parameter
 * @param <I> the type parameter
 */
public abstract class AbstractNextCodeService<T extends NextCodeModel, I extends Serializable> implements INextCodeService<T> {

    /**
     * Next code repository next code repository.
     *
     * @return the next code repository
     */
    public abstract NextCodeRepository<T, I> nextCodeRepository();

    @Override
    public Optional<T> getByEntity(String entity) {
        return nextCodeRepository().findByEntity(entity);
    }

    @Override
    public Optional<T> getByDomainAndEntityAndAttribute(String domain, String entity, String attribute) {
        return nextCodeRepository().findByDomainIgnoreCaseAndEntityAndAttribute(domain, entity, attribute);
    }

    @Override
    @Transactional
    public void increment(String domain, String entity, Integer increment) {
        nextCodeRepository().increment(domain, entity, increment);
        nextCodeRepository().flush(); // Ensures the changes are immediately persisted
    }

    @Override
    @Transactional
    public T saveAndFlush(T appNextCode) {
        return nextCodeRepository().saveAndFlush(appNextCode);
    }

    @Override
    @Transactional
    public T save(T appNextCode) {
        return nextCodeRepository().save(appNextCode);
    }
}
