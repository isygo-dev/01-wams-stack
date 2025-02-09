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
        return createCodeGenerator()
                .filter(generator -> StringUtils.hasText(generator.getEntity()))
                .map(generator -> {
                    String nextCode = generator.nextCode().getCode();
                    nextCodeService.saveAndFlush(generator);
                    return nextCode;
                });
    }

    private Optional<T> createCodeGenerator() {
        return initCodeGenerator()
                .filter(generator -> StringUtils.hasText(generator.getEntity()))
                .flatMap(generator -> {
                    Optional<T> existingGenerator = nextCodeService.getByDomainAndEntityAndAttribute(
                            generator.getDomain(), generator.getEntity(), generator.getAttribute());
                    return existingGenerator.isPresent() ? existingGenerator : Optional.ofNullable(nextCodeService.save(generator));
                });
    }

    @Override
    public <E extends IIdEntity> E beforePersist(E entity) {
        if (entity instanceof ICodifiable codifiable && !StringUtils.hasText(codifiable.getCode())) {
            getNextCode().ifPresent(codifiable::setCode);
        }
        return entity;
    }
}