package eu.isygoit.com.rest.service.impl.cassandra;

import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.com.rest.service.impl.utils.CrudServiceUtils;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyListException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.OperationNotAllowedException;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.model.AssignableDomain;
import eu.isygoit.model.AssignableId;
import eu.isygoit.model.jakarta.Cancelable;
import eu.isygoit.repository.JpaPagingAndSortingAssignableDomainRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.NotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;

/**
 * The type Cassandra crud service.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CassandraCrudService<I extends Serializable,
        E extends AssignableId,
        R extends CassandraRepository<E, I>>
        extends CrudServiceUtils<I, E, R>
        implements ICrudServiceMethod<I, E> {

    @Override
    @Transactional(readOnly = true)
    public Long count() {
        log.debug("Counting all entities of type {}", getPersistentClass().getSimpleName());
        return logAndExecute("Counting all entities of type {}", () -> getRepository().count());
    }

    @Override
    @Transactional(readOnly = true)
    public Long count(String domain) {
        log.debug("Counting entities in domain: {}", domain);
        if (getRepository() instanceof JpaPagingAndSortingAssignableDomainRepository<?, ?> repo) {
            return logAndExecute("Counting entities in domain: {}", () -> repo.countByDomainIgnoreCase(domain));
        }
        throw new UnsupportedOperationException("Not a SAAS entity/repository: " + getRepository().getClass().getSimpleName());
    }

    @Override
    public boolean existsById(I id) {
        log.debug("Checking if entity exists with ID: {}", id);
        return logAndExecute("Checking if entity exists with ID: {}", () -> getRepository().existsById(id));
    }

    @Override
    @Transactional
    public E create(E object) {
        log.debug("Creating entity: {}", object);
        validateObject(object, false);
        return logAndExecute("Creating entity: {}", () -> afterCreate(getRepository().save(processObject(object))));
    }

    @Override
    public List<E> create(List<E> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        List<E> createdObjects = new ArrayList<>();
        for (E object : objects) {
            log.debug("Creating entity: {}", object);
            validateObject(object, false);
            createdObjects.add(this.create(object));
        }
        return createdObjects;
    }

    @Override
    @Transactional
    public E update(E object) {
        log.debug("Updating entity: {}", object);
        validateObject(object, true);
        return logAndExecute("Updating entity: {}", () -> afterUpdate(getRepository().save(processObject(object))));
    }

    @Override
    @Transactional
    public List<E> update(List<E> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        List<E> updatedObjects = new ArrayList<>();
        for (E object : objects) {
            log.debug("Updating entity: {}", object);
            validateObject(object, true);
            updatedObjects.add(this.update(object));
        }

        return updatedObjects;
    }

    @Override
    @Transactional
    public void delete(String senderDomain, I id) {
        log.debug("Deleting entity with ID: {}", id);
        validateObjectId(id);
        var object = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found for deletion: " + id));

        checkOperationIsAllowedForDomain(senderDomain, id, object);
        beforeDelete(id);

        if (Cancelable.class.isAssignableFrom(getPersistentClass())) {
            cancelEntity(object);
        } else {
            getRepository().deleteById(id);
        }
        afterDelete(id);
    }

    @Override
    @Transactional
    public void delete(String senderDomain, List<E> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        if (AssignableDomain.class.isAssignableFrom(getPersistentClass()) && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            objects.forEach(object -> {
                if (!senderDomain.equals(((AssignableDomain) object).getDomain())) {
                    throw new OperationNotAllowedException("Delete operation not allowed for entity with ID: " + object.getId());
                }
            });
        }

        this.beforeDelete(objects);
        getRepository().deleteAll(objects);
        this.afterDelete(objects);
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> findAll() {
        log.debug("Fetching all entities of type {}", getPersistentClass().getSimpleName());
        return logAndExecute("Fetching all entities of type: {}", () -> Optional.of(getRepository().findAll())
                .filter(list -> !list.isEmpty())
                .orElseGet(List::of));
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> findAll(Pageable pageable) {
        log.debug("Retrieving entities from the database with pagination: {}", pageable);
        Slice<E> page = getRepository().findAll(pageable);
        if (page.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        return this.afterFindAll(page.getContent());
    }

    @Override
    public List<E> findAll(String domain) throws NotSupportedException {
        log.debug("Retrieving entities by domain: {}", domain);
        if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                && getRepository() instanceof JpaPagingAndSortingAssignableDomainRepository jpaPagingAndSortingAssignableDomainRepository) {
            List<E> list = jpaPagingAndSortingAssignableDomainRepository.findByDomainIgnoreCase(domain);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.EMPTY_LIST;
            }
            return this.afterFindAll(list);
        } else {
            throw new NotSupportedException("Find all by domain is not supported for: " + getPersistentClass().getSimpleName());
        }
    }

    @Override
    public List<E> findAll(String domain, Pageable pageable) throws NotSupportedException {
        log.debug("Retrieving entities by domain with pagination: {} for domain {}", pageable, domain);
        if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                && getRepository() instanceof JpaPagingAndSortingAssignableDomainRepository jpaPagingAndSortingAssignableDomainRepository) {
            Page<E> page = jpaPagingAndSortingAssignableDomainRepository.findByDomainIgnoreCase(domain, pageable);
            if (page.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            return this.afterFindAll(page.getContent());
        } else {
            throw new NotSupportedException("Find all by domain with pagination is not supported for: " + getPersistentClass().getSimpleName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<E> findById(I id) throws ObjectNotFoundException {
        log.debug("Retrieving entity by ID: {}", id);
        Optional.ofNullable(id)
                .orElseThrow(() -> new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED));

        Optional<E> optional = getRepository().findById(id);
        return optional.map(entity -> Optional.ofNullable(afterFindById(entity)))
                .orElse(Optional.empty());
    }

    @Override
    @Transactional
    public E saveOrUpdate(E object) {
        log.debug("Saving or updating entity: {}", object);
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (Objects.isNull(object.getId())) { // to create
            return this.create(object);
        } else { //to update
            return this.update(object);
        }
    }

    @Override
    @Transactional
    public List<E> saveOrUpdate(List<E> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        List<E> updatedObjects = new ArrayList<>();
        objects.forEach(object -> {
            updatedObjects.add(this.saveOrUpdate(object));
        });

        return updatedObjects;
    }

    // Hook methods for customization during CRUD operations
    @Override
    public E beforeUpdate(E object) {
        return object;
    }

    @Override
    public E afterUpdate(E object) {
        return object;
    }

    @Override
    public List<E> afterFindAll(List<E> list) {
        return list;
    }

    @Override
    public E afterFindById(E object) {
        return object;
    }

    @Override
    public List<E> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria) {
        return null;
    }

    @Override
    public List<E> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria, PageRequest pageRequest) {
        return null;
    }

    @Override
    public E beforeCreate(E object) {
        return object;
    }

    @Override
    public E afterCreate(E object) {
        return object;
    }

    @Override
    public void beforeDelete(I id) {
    }

    @Override
    public void afterDelete(I id) {
    }

    private E processObject(E object) {
        log.debug("Processing object before saving: {}", object);
        object = beforeCreate(object);
        object = this.processCodeAssignable(object);
        return object;
    }

    /**
     * Cancel entity.
     *
     * @param object the object
     */
    public void cancelEntity(E object) {
        //For Soft delete
        log.debug("Cancelling entity with ID: {}", object.getId());
        ((Cancelable) object).setCheckCancel(true);
        ((Cancelable) object).setCancelDate(new Date());
        update(object);
    }
}