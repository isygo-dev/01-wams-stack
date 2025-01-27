package eu.isygoit.service;

import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.service.nextCode.INextCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * The type Abstract service.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class AbstractService<T extends NextCodeModel> implements IService {

    @Autowired
    private INextCodeService<T> nextCodeService;


    @Override
    public Optional<T> initCodeGenerator() {
        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<String> getNextCode() {
        Optional<T> optional = this.createCodeGenerator();
        Optional<String> returnOptional = Optional.empty();
        if (optional.isPresent() && StringUtils.hasText(optional.get().getEntity())) {
            returnOptional = Optional.ofNullable(optional.get().nextCode().getCode());
            nextCodeService.saveAndFlush(optional.get());
        } else {
            log.error("<Error>: Class code generator could not be initialized");
        }

        return returnOptional;
    }

    private final Optional<T> createCodeGenerator() {
        Optional<T> defaultNextCode = this.initCodeGenerator();
        if (defaultNextCode.isPresent() && StringUtils.hasText(defaultNextCode.get().getEntity())) {
            T initNextCodeGen = defaultNextCode.get();
            Optional<T> optional = nextCodeService.findByDomainAndEntityAndAttribute(initNextCodeGen.getDomain()
                    , initNextCodeGen.getEntity(), initNextCodeGen.getAttribute());
            if (optional.isPresent()) {
                return optional;
            } else {
                return Optional.ofNullable(nextCodeService.save(initNextCodeGen));
            }
        }
        return Optional.empty();
    }

    @Override
    public <E extends IIdEntity> E beforePersist(E entity) {
        if (entity instanceof ICodifiable codifiable && !StringUtils.hasText(((ICodifiable) entity).getCode())) {
            this.getNextCode().ifPresent(code -> codifiable.setCode(code));
        }
        return entity;
    }
}
