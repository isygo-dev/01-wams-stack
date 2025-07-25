package eu.isygoit.service;

import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.repository.NextCodeRepository;
import eu.isygoit.service.nextCode.ICodeGeneratorService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * The type Abstract next code api.
 *
 * @param <T> the type parameter
 */
public abstract class AbstractCodeGeneratorService<T extends NextCodeModel>
        implements ICodeGeneratorService<T> {

    /**
     * Next code repository next code repository.
     *
     * @return the next code repository
     */
    public abstract NextCodeRepository nextCodeRepository();

    @Override
    public Optional<T> findByEntity(String entity) {
        return nextCodeRepository().findByEntity(entity);
    }

    @Override
    public Optional<T> findByTenantAndEntityAndAttribute(String tenant, String entity, String attribute) {
        return nextCodeRepository().findByTenantIgnoreCaseAndEntityAndAttribute(tenant, entity, attribute);
    }

    @Override
    @Transactional
    public void increment(String tenant, String entity, Integer increment) {
        nextCodeRepository().increment(tenant, entity, increment);
        nextCodeRepository().flush();
    }

    public T saveAndFlush(T appNextCode) {
        return (T) nextCodeRepository().saveAndFlush(appNextCode);
    }

    public T save(T appNextCode) {
        return (T) nextCodeRepository().save(appNextCode);
    }
}
