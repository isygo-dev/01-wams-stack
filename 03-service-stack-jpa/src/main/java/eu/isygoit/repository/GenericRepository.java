package eu.isygoit.repository;

import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.model.AssignableId;
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
public class GenericRepository {

    /**
     * The Repositories.
     */
    Repositories repositories = null;

    /**
     * Instantiates a new Generic repository.
     *
     * @param appContext the app context
     */
    public GenericRepository(@Autowired WebApplicationContext appContext) {
        repositories = new Repositories(appContext);
    }

    /**
     * Gets repository.
     *
     * @param domainClass the domain class
     * @return the repository
     */
    public JpaRepository getRepository(Class<?> domainClass) {
        Optional<Object> optional = repositories.getRepositoryFor(domainClass);
        if (optional.isPresent()) {
            return (JpaRepository) optional.get();
        }
        throw new JpaRepositoryNotDefinedException("for entity " + domainClass.getSimpleName());
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
     * Save object.
     *
     * @param entity the entity
     * @return the object
     */
    public Object save(AssignableId entity) {
        return getRepository(entity.getClass()).save(entity);
    }

    /**
     * Find all object.
     *
     * @param entity the entity
     * @return the object
     */
    public Object findAll(AssignableId entity) {
        return getRepository(entity.getClass()).findAll();
    }

    /**
     * Delete.
     *
     * @param entity the entity
     */
    public void delete(AssignableId entity) {
        getRepository(entity.getClass()).delete(entity);
    }
}
