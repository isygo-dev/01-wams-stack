package eu.isygoit.com.rest.service.cassandra;

import eu.isygoit.com.rest.service.CrudServiceUtils;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.tenancy.ICrudTenantServiceEvents;
import eu.isygoit.com.rest.service.tenancy.ICrudTenantServiceMethods;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.OperationNotAllowedException;
import eu.isygoit.exception.OperationNotSupportedException;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.CancelableEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public abstract class CassandraCrudTenantService<I extends Serializable,
        T extends IIdAssignable<I> & ITenantAssignable,
        R extends CassandraRepository<T, I>>
        extends CrudServiceUtils<I, T, R>
        implements ICrudTenantServiceMethods<I, T>, ICrudTenantServiceEvents<I, T>, ICrudServiceUtils<I, T> {

    //Attention !!! should get the class type of th persist entity
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    @Transactional(readOnly = true)
    public Long count(String tenant) {
        if (repository() instanceof JpaPagingAndSortingTenantAssignableRepository jpaPagingAndSortingTenantAssignableRepository) {
            return jpaPagingAndSortingTenantAssignableRepository.countByTenantIgnoreCase(tenant);
        } else {
            throw new UnsupportedOperationException("this is not a SAS entity/repository: " + repository().getClass().getSimpleName());
        }
    }

    @Override
    public T beforeCreate(String tenant, T object) {
        return object;
    }

    @Override
    public T afterCreate(String tenant, T object) {
        return object;
    }

    @Override
    @Transactional
    public void deleteBatch(String tenant, List<T> objects) {
        validateListNotEmpty(objects);

        if (!TenantConstants.SUPER_TENANT_NAME.equals(tenant)) {
            objects.forEach(object -> {
                if (!tenant.equals((object).getTenant())) {
                    throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " with id: " + object.getId());
                }
            });
        }

        this.beforeDelete(tenant, objects);
        repository().deleteAll(objects);
        this.afterDelete(tenant, objects);
    }

    @Override
    @Transactional
    public void delete(String tenant, I id) {
        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        Optional<T> optional = this.findById(tenant, id);
        if (optional.isPresent()) {
            T object = optional.get();
            if (!TenantConstants.SUPER_TENANT_NAME.equals(tenant)) {
                if (!tenant.equals(object.getTenant())) {
                    throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " with id: " + id);
                }
            }

            this.beforeDelete(tenant, id);
            handleEntityDeletion(object);
            this.afterDelete(tenant, id);
        } else {
            throw new ObjectNotFoundException(" with id: " + id);
        }
    }

    @Override
    public boolean existsById(String tenant, I id) {
        return false;
    }

    @Override
    public T create(String tenant, T object) {
        return null;
    }

    @Override
    public List<T> createBatch(String tenant, List<T> objects) {
        return List.of();
    }

    @Override
    public Optional<T> findById(String tenant, I id) throws ObjectNotFoundException {
        return Optional.empty();
    }

    @Override
    public T saveOrUpdate(String tenant, T object) {
        return null;
    }

    @Override
    public List<T> saveOrUpdate(String tenant, List<T> objects) {
        return List.of();
    }

    @Override
    public T update(String tenant, T object) {
        return null;
    }

    @Override
    public List<T> updateBatch(String tenant, List<T> objects) {
        return List.of();
    }

    @Override
    public void beforeDelete(String tenant, I id) {
    }

    @Override
    public void afterDelete(String tenant, I id) {
    }

    @Override
    public void beforeDelete(String tenant, List<T> objects) {
    }

    @Override
    public void afterDelete(String tenant, List<T> objects) {
    }
    

    @Override
    public List<T> findAll(String tenant) {
        if (repository() instanceof JpaPagingAndSortingTenantAssignableRepository jpaPagingAndSortingTenantAssignableRepository) {
            List<T> list = jpaPagingAndSortingTenantAssignableRepository.findByTenantIgnoreCase(tenant);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.EMPTY_LIST;
            }
            return this.afterFindAll(tenant, list);
        } else {
            throw new OperationNotSupportedException("find all by tenant for :" + persistentClass.getSimpleName());
        }
    }

    @Override
    public List<T> findAll(String tenant, Pageable pageable) {
        if (repository() instanceof JpaPagingAndSortingTenantAssignableRepository jpaPagingAndSortingTenantAssignableRepository) {
            Page<T> page = jpaPagingAndSortingTenantAssignableRepository.findByTenantIgnoreCase(tenant, pageable);
            if (page.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            return this.afterFindAll(tenant, page.getContent());
        } else {
            throw new OperationNotSupportedException("find all by tenant for :" + persistentClass.getSimpleName());
        }
    }

    @Override
    public T beforeUpdate(String tenant, T object) {
        return object;
    }

    @Override
    public T afterUpdate(String tenant, T object) {
        return object;
    }

    @Override
    public List<T> afterFindAll(String tenant, List<T> list) {
        return list;
    }

    @Override
    public T afterFindById(String tenant, T object) {
        return object;
    }

    @Override
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria) {
        return null;
    }

    @Override
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria, PageRequest pageRequest) {
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
