package eu.isygoit.com.rest.service.cassandra;

import eu.isygoit.com.rest.service.CrudServiceUtils;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceMethods;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.OperationNotAllowedException;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.CancelableEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.*;

/**
 * The type Cassandra crud api.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CassandraCrudService<I extends Serializable,
        T extends IIdAssignable<I>,
        R extends CassandraRepository<T, I>>
        extends CrudServiceUtils<I, T, R>
        implements ICrudServiceMethods<I, T>, ICrudServiceEvents<I, T>, ICrudServiceUtils<I, T> {

    //Attention !!! should get the class type of th persist entity
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    @Transactional(readOnly = true)
    public Long count() {
        return repository().count();
    }

    @Override
    public boolean existsById(I id) {
        return repository().existsById(id);
    }

    @Override
    public T beforeCreate(T object) {
        return object;
    }

    @Override
    public T afterCreate(T object) {
        return object;
    }

    @Override
    @Transactional
    public T create(T object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (object.getId() == null) {
            object = this.beforeCreate(object);
            assignCodeIfEmpty(object);
            return this.afterCreate(repository().save(object));
        } else {
            throw new EntityExistsException();
        }
    }

    @Override
    public List<T> createBatch(List<T> objects) {
        validateListNotEmpty(objects);

        List<T> createdObjects = new ArrayList<>();
        objects.forEach(object -> {
            createdObjects.add(this.create(object));
        });

        return createdObjects;
    }

    @Override
    @Transactional
    public T update(T object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (object.getId() != null) {
            object = this.beforeUpdate(object);
            assignCodeIfEmpty(object);
            return this.afterUpdate(repository().save(object));
        } else {
            throw new EntityNotFoundException();
        }
    }

    @Override
    @Transactional
    public List<T> updateBatch(List<T> objects) {
        validateListNotEmpty(objects);
        List<T> updatedObjects = new ArrayList<>();
        objects.forEach(object -> {
            updatedObjects.add(this.update(object));
        });

        return updatedObjects;
    }

    @Override
    @Transactional
    public void deleteBatch(List<T> objects) {
        if (ITenantAssignable.class.isAssignableFrom(persistentClass)) {
            throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " should use SAAS delete");
        }

        validateListNotEmpty(objects);
        this.beforeDelete(objects);
        repository().deleteAll(objects);
        this.afterDelete(objects);
    }

    @Override
    @Transactional
    public void delete(I id) {
        if (ITenantAssignable.class.isAssignableFrom(persistentClass)) {
            throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " should use SAAS delete");
        }

        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        Optional<T> optional = this.findById(id);
        if (optional.isPresent()) {
            T object = optional.get();
            this.beforeDelete(id);
            handleEntityDeletion(object);
            this.afterDelete(id);
        } else {
            throw new ObjectNotFoundException("with id " + id);
        }
    }

    @Override
    public void beforeDelete(I id) {
    }

    @Override
    public void afterDelete(I id) {
    }

    @Override
    public void beforeDelete(List<T> objects) {
    }

    @Override
    public void afterDelete(List<T> objects) {
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        if (ITenantAssignable.class.isAssignableFrom(persistentClass)
                && repository() instanceof JpaPagingAndSortingTenantAssignableRepository) {
            log.warn("Find all give vulnerability to SAS entity...");
        }

        List<T> list = repository().findAll();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }

        return this.afterFindAll(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(Pageable pageable) {
        if (ITenantAssignable.class.isAssignableFrom(persistentClass)
                && repository() instanceof JpaPagingAndSortingTenantAssignableRepository) {
            log.warn("Find all give vulnerability to SAS entity...");
        }

        Slice<T> page = repository().findAll(pageable);
        if (page.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        return this.afterFindAll(page.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(I id) throws ObjectNotFoundException {
        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        // Retrieve the entity and apply afterFindById if present
        return repository().findById(id)
                .map(entity -> afterFindById(entity));  // Apply afterFindById if value is present
    }

    @Override
    @Transactional
    public T saveOrUpdate(T object) {
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
    public List<T> saveOrUpdate(List<T> objects) {
        validateListNotEmpty(objects);

        List<T> updatedObjects = new ArrayList<>();
        objects.forEach(object -> {
            updatedObjects.add(this.saveOrUpdate(object));
        });

        return updatedObjects;
    }

    @Override
    public T beforeUpdate(T object) {
        return object;
    }

    @Override
    public T afterUpdate(T object) {
        return object;
    }

    @Override
    public List<T> afterFindAll(List<T> list) {
        return list;
    }

    @Override
    public T afterFindById(T object) {
        return object;
    }

    @Override
    public List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria) {
        return null;
    }

    @Override
    public List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria, PageRequest pageRequest) {
        return null;
    }

    @Override
    public List<T> getByIdIn(List<I> ids) {
        return null;
    }

    /**
     * Handles entity deletion, supporting soft deletion for CancelableEntity.
     *
     * @param object the entity to delete
     */
    private void handleEntityDeletion(T object) {
        log.debug("Handling deletion for {} entity with ID: {}", persistentClass.getSimpleName(), object.getId());
        if (object instanceof CancelableEntity cancelable && !cancelable.getCheckCancel()) {
            cancelable.setCheckCancel(true);
            cancelable.setCancelDate(Date.from(Instant.now()));
            repository().save(object);
        } else {
            repository().delete(object);
        }
    }
}
