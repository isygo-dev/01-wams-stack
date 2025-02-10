package eu.isygoit.service.nextCode;

import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.repository.NextCodeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Optional;

/**
 * Abstract service for managing the next code values for different entities.
 * <p>
 * This service provides common operations like retrieving, saving, and incrementing next codes
 * for entities and attributes. The repository used is abstracted and needs to be provided
 * by the subclass.
 * </p>
 *
 * @param <T> the type of the next code model
 * @param <I> the type of the identifier (should be serializable)
 */
public abstract class AbstractNextCodeService<T extends NextCodeModel, I extends Serializable> implements INextCodeService<T> {

    /**
     * Retrieves the next code repository for the specific entity model.
     * Subclasses must implement this to return the correct repository.
     * <p>
     * This method now returns an Optional, in case the repository might not be available.
     * </p>
     *
     * @return an Optional containing the next code repository, or empty if not available
     */
    protected Optional<NextCodeRepository<T, I>> getNextCodeRepository() {
        return Optional.ofNullable(getRepositoryInstance());
    }

    /**
     * Abstract method that subclasses must implement to provide the actual repository instance.
     *
     * @return the next code repository instance, or null if not available
     */
    protected abstract NextCodeRepository<T, I> getRepositoryInstance();

    /**
     * Finds the next code entry by its entity name.
     *
     * @param entity the name of the entity
     * @return an Optional containing the found next code, or empty if not found
     */
    @Override
    public Optional<T> getByEntity(String entity) {
        return getNextCodeRepository().flatMap(repository -> repository.findByEntity(entity));
    }

    /**
     * Finds the next code entry by domain, entity, and attribute.
     *
     * @param domain    the domain of the next code
     * @param entity    the entity name
     * @param attribute the attribute of the next code
     * @return an Optional containing the found next code, or empty if not found
     */
    @Override
    public Optional<T> getByDomainEntityAndAttribute(String domain, String entity, String attribute) {
        return getNextCodeRepository().flatMap(repository ->
                repository.findByDomainIgnoreCaseAndEntityAndAttribute(domain, entity, attribute)
        );
    }

    /**
     * Increments the next code for a given domain and entity by the specified amount.
     * <p>
     * This operation ensures that the changes are immediately persisted to the database.
     * </p>
     *
     * @param domain    the domain of the next code
     * @param entity    the entity name
     * @param increment the value to increment the next code by
     */
    @Override
    @Transactional
    public void incrementNextCode(String domain, String entity, Integer increment) {
        getNextCodeRepository()
                .ifPresent(repository -> {
                    repository.increment(domain, entity, increment);
                    repository.flush();
                });
    }

    /**
     * Saves and immediately flushes the given next code entity.
     *
     * @param nextCode the next code entity to save
     * @return the saved next code entity
     */
    @Override
    @Transactional
    public T saveAndFlush(T nextCode) {
        return getNextCodeRepository()
                .map(repository -> repository.saveAndFlush(nextCode))
                .orElseThrow(() -> new IllegalStateException("Repository is not available"));
    }

    /**
     * Saves the given next code entity without immediately flushing.
     *
     * @param nextCode the next code entity to save
     * @return the saved next code entity
     */
    @Override
    @Transactional
    public T saveNextCode(T nextCode) {
        return getNextCodeRepository()
                .map(repository -> repository.save(nextCode))
                .orElseThrow(() -> new IllegalStateException("Repository is not available"));
    }
}