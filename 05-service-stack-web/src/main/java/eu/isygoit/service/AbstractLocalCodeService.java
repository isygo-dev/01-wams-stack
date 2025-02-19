package eu.isygoit.service;

import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.repository.NextCodeRepository;
import eu.isygoit.service.nextCode.ILocalCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Optional;

/**
 * The type Abstract local code service.
 *
 * @param <I> the type parameter
 * @param <C> the type parameter
 */
@Slf4j
public abstract class AbstractLocalCodeService<C extends NextCodeModel,
        I extends Serializable>
        implements ILocalCodeService<C> {

    /**
     * Next code repository next code repository.
     *
     * @return the next code repository
     */
    public abstract NextCodeRepository<C, I> nextCodeRepository();

    @Override
    public Optional<C> findByEntity(String entity) {
        return nextCodeRepository().findByEntity(entity);
    }

    @Override
    public Optional<C> findByDomainAndEntityAndAttribute(String domain, String entity, String attribute) {
        return nextCodeRepository().findByDomainIgnoreCaseAndEntityAndAttribute(domain, entity, attribute);
    }

    @Override
    @Transactional
    public void increment(String domain, String entity, Integer increment) {
        nextCodeRepository().increment(domain, entity, increment);
        nextCodeRepository().flush();
        log.info("Incremented code for domain '{}', entity '{}', by '{}'", domain, entity, increment);
    }

    public C saveAndFlush(C appNextCode) {
        C saved = nextCodeRepository().saveAndFlush(appNextCode);
        log.info("Saved and flushed NextCode for entity '{}'", appNextCode.getEntity());
        return saved;
    }

    public C save(C appNextCode) {
        C saved = nextCodeRepository().save(appNextCode);
        log.info("Saved NextCode for entity '{}'", appNextCode.getEntity());
        return saved;
    }
}