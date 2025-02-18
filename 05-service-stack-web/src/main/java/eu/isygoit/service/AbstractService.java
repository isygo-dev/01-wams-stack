package eu.isygoit.service;

import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.service.nextCode.ILocalCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * The type Abstract service.
 *
 * @param <C> the type parameter
 */
@Slf4j
public abstract class AbstractService<C extends NextCodeModel> implements IService {

    @Autowired
    private ILocalCodeService<C> nextCodeService;

    @Override
    public abstract C initCodeGenerator();

    @Override
    @Transactional
    public Optional<String> getNextCode() {
        return createCodeGenerator()
                .filter(nextCode -> StringUtils.hasText(nextCode.getEntity()))
                .map(nextCode -> {
                    var newCode = nextCode.nextCode().getCode();
                    nextCodeService.saveAndFlush(nextCode);
                    log.info("Generated new code: {}", newCode);
                    return newCode;
                })
                .or(() -> {
                    log.error("Error: Class code generator could not be initialized");
                    return Optional.empty();  // Return Optional.empty() instead of null
                });
    }

    private Optional<C> createCodeGenerator() {
        var defaultNextCode = initCodeGenerator();
        return Optional.ofNullable(defaultNextCode)
                .filter(code -> StringUtils.hasText(code.getEntity()))
                .flatMap(code -> {
                    var nextCodeModel = nextCodeService.findByDomainAndEntityAndAttribute(code.getDomain(), code.getEntity(), code.getAttribute());
                    return nextCodeModel.isPresent() ? nextCodeModel : Optional.of(nextCodeService.save(defaultNextCode));
                });
    }
}