package eu.isygoit.service;

import eu.isygoit.model.IAssignableCode;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.service.nextCode.INextCodeService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * The type Abstract service.
 *
 * @param <E> the type parameter
 */
@Slf4j
public abstract class AbstractService<E extends NextCodeModel> implements IService {

    @Autowired
    private INextCodeService<E> nextCodeService;


    @Override
    public E initCodeGenerator() {
        return null;
    }

    @Override
    @Transactional
    public String getNextCode() {
        NextCodeModel defaultNextCode = this.createCodeGenerator();
        if (defaultNextCode != null && StringUtils.hasText(defaultNextCode.getEntity())) {
            String newCode = defaultNextCode.nextCode().getCode();
            nextCodeService.saveAndFlush((E) defaultNextCode);
            return newCode;
        } else {
            log.error("<Error>: Class code generator could not be initialized");
        }
        return null;
    }

    private final E createCodeGenerator() {
        E defaultNextCode = this.initCodeGenerator();
        if (defaultNextCode != null && StringUtils.hasText(defaultNextCode.getEntity())) {
            E nextCodeModel = nextCodeService.findByDomainAndEntityAndAttribute(defaultNextCode.getDomain()
                    , defaultNextCode.getEntity(), defaultNextCode.getAttribute());
            if (nextCodeModel == null) {
                return nextCodeService.save(initCodeGenerator());
            } else {
                return nextCodeModel;
            }
        }
        return null;
    }

    @Override
    public <E extends IIdEntity> E beforePersist(E entity) {
        if (entity instanceof IAssignableCode codifiable && !StringUtils.hasText(((IAssignableCode) entity).getCode())) {
            codifiable.setCode(this.getNextCode());
        }
        return entity;
    }
}
