package eu.isygoit.repository;

import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.model.IIdAssignable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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
     * @param entityClass the entity class
     * @return the repository
     */
    public JpaRepository getRepository(Class<?> entityClass) {
        Optional<Object> optional = repositories.getRepositoryFor(entityClass);
        if (optional.isPresent()) {
            return (JpaRepository) optional.get();
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
    public JpaRepository getRepository(String className) throws ClassNotFoundException {
        Class itemClass = Class.forName(className);
        return this.getRepository(itemClass);
    }

    /**
     * Save id assignable.
     *
     * @param entity the entity
     * @return the id assignable
     */
    public IIdAssignable save(IIdAssignable entity) {
        return (IIdAssignable) getRepository(entity.getClass()).save(entity);
    }

    /**
     * Find by id optional.
     *
     * @param entity the entity
     * @return the optional
     */
    public Optional<IIdAssignable> findById(IIdAssignable entity) {
        return getRepository(entity.getClass()).findById(entity.getId());
    }

    /**
     * Find all id assignable.
     *
     * @param entity the entity
     * @return the id assignable
     */
    public IIdAssignable findAll(IIdAssignable entity) {
        return (IIdAssignable) getRepository(entity.getClass()).findAll();
    }

    /**
     * Delete.
     *
     * @param entity the entity
     */
    public void delete(IIdAssignable entity) {
        getRepository(entity.getClass()).delete(entity);
    }
}
