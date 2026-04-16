package eu.isygoit.repository;

import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.model.IIdAssignable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.Serializable;
import java.util.Optional;

/**
 * The type Generic repository.
 */
@Service
@Transactional
public class GenericRepositoryService {

    /**
     * The Repositories.
     */
    Repositories repositories = null;

    /**
     * Instantiates a new Generic repository.
     *
     * @param appContext the app context
     */
    public GenericRepositoryService(@Autowired WebApplicationContext appContext) {
        repositories = new Repositories(appContext);
    }

    /**
     * Gets repository.
     *
     * @param <T>         the entity type
     * @param <I>         the id type
     * @param entityClass the entity class
     * @return the repository
     */
    @SuppressWarnings("unchecked")
    public <T extends IIdAssignable<I>, I extends Serializable> JpaRepository<T, I> getRepository(Class<T> entityClass) {
        Optional<Object> optional = repositories.getRepositoryFor(entityClass);
        if (optional.isPresent()) {
            return (JpaRepository<T, I>) optional.get();
        }
        throw new JpaRepositoryNotDefinedException("for entity " + entityClass.getSimpleName());
    }

    /**
     * Gets repository.
     *
     * @param className the class name
     * @return the repository
     * @throws ClassNotFoundException the class not found exception
     */
    @SuppressWarnings("unchecked")
    public JpaRepository<? extends IIdAssignable<?>, ? extends Serializable> getRepository(String className) throws ClassNotFoundException {
        Class<?> itemClass = Class.forName(className);
        return this.getRepository((Class<IIdAssignable<Serializable>>) itemClass);
    }

    /**
     * Save id assignable.
     *
     * @param <T>    the entity type
     * @param <I>    the id type
     * @param entity the entity
     * @return the id assignable
     */
    @SuppressWarnings("unchecked")
    public <T extends IIdAssignable<I>, I extends Serializable> T save(T entity) {
        return (T) getRepository((Class<T>) entity.getClass()).save(entity);
    }

    /**
     * Find by id optional.
     *
     * @param <T>    the entity type
     * @param <I>    the id type
     * @param entity the entity
     * @return the optional
     */
    @SuppressWarnings("unchecked")
    public <T extends IIdAssignable<I>, I extends Serializable> Optional<T> findById(T entity) {
        return getRepository((Class<T>) entity.getClass()).findById(entity.getId());
    }

    /**
     * Find all id assignable.
     *
     * @param <T>    the entity type
     * @param <I>    the id type
     * @param entity the entity
     * @return the list of id assignable
     */
    @SuppressWarnings("unchecked")
    public <T extends IIdAssignable<I>, I extends Serializable> Iterable<T> findAll(T entity) {
        return getRepository((Class<T>) entity.getClass()).findAll();
    }

    /**
     * Delete.
     *
     * @param <T>    the entity type
     * @param <I>    the id type
     * @param entity the entity
     */
    @SuppressWarnings("unchecked")
    public <T extends IIdAssignable<I>, I extends Serializable> void delete(T entity) {
        getRepository((Class<T>) entity.getClass()).delete(entity);
    }
}
