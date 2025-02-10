package eu.isygoit.service;

import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.service.nextCode.INextCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * The type Abstract service.
 *
 * @param <T> the type parameter extending {@link NextCodeModel}.
 *
 *            <p>
 *            This abstract class provides base functionality for services that require
 *            code generation. Inherited services should override the {@link #getNextCodeService()}
 *            method to provide the specific service for handling next code logic.
 *            Inherited services should autowire:
 *            private INextCodeService<T> nextCodeService;
 *            </p>
 */
@Slf4j
public abstract class AbstractService<T extends NextCodeModel> implements IService {

    /**
     * Abstract method to retrieve the next code service. Returns an Optional to handle potential absence of service.
     *
     * @return an {@link Optional} containing the next code service, if present
     */
    public abstract Optional<INextCodeService<T>> getNextCodeService();

    /**
     * Initializes the code generator, can be overridden in subclasses.
     *
     * @return an {@link Optional} containing the code generator if applicable
     */
    @Override
    public Optional<T> initializeCodeGenerator() {
        // Default implementation returns an empty Optional, subclasses may override
        return Optional.empty();
    }

    /**
     * Retrieves the next code for the entity. It first attempts to create a code generator
     * and then fetches the next code from the generator.
     *
     * @return an {@link Optional} containing the next code if available
     */
    @Override
    @Transactional
    public Optional<String> generateNextCode() {
        return createOrFetchCodeGenerator()
                .filter(generator -> StringUtils.hasText(generator.getEntity())) // Ensures entity is not empty
                .map(generator -> {
                    String nextCode = generator.nextCode().getCode(); // Fetch the next code
                    getNextCodeService().ifPresent(service -> service.saveAndFlush(generator)); // Persist the generator, if service is available
                    return nextCode;
                });
    }

    /**
     * Creates the code generator, either retrieving an existing one or saving a new one.
     *
     * @return an {@link Optional} containing the code generator
     */
    private Optional<T> createOrFetchCodeGenerator() {
        return initializeCodeGenerator() // Get the generator from init method
                .filter(generator -> StringUtils.hasText(generator.getEntity())) // Ensure the entity is valid
                .flatMap(generator -> {
                    // Try to fetch the existing generator by domain, entity, and attribute
                    Optional<T> existingGenerator = getNextCodeService()
                            .flatMap(service -> service.getByDomainEntityAndAttribute(
                                    generator.getDomain(), generator.getEntity(), generator.getAttribute()));
                    // If an existing generator exists, return it, otherwise create and persist a new one
                    return existingGenerator.isPresent() ? existingGenerator : Optional.ofNullable(getNextCodeService().flatMap(service -> Optional.ofNullable(service.saveNextCode(generator))).orElse(null));
                });
    }

    /**
     * Pre-persistence hook to assign a generated code to an entity if applicable.
     * This method checks if the entity is {@link ICodifiable} and if it doesn't already have a code.
     * If the entity is codifiable and doesn't have a code, it attempts to fetch the next code.
     *
     * @param entity the entity to be persisted
     * @param <E>    the type of the entity, extending {@link IIdEntity}
     * @return the entity with a potential code set
     */
    @Override
    public <E extends IIdEntity> E beforeSave(E entity) {
        // Check if the entity is codifiable and doesn't have a code
        if (entity instanceof ICodifiable codifiable && !StringUtils.hasText(codifiable.getCode())) {
            // Set the code if a next code is available
            generateNextCode().ifPresent(codifiable::setCode);
        }
        return entity;
    }
}