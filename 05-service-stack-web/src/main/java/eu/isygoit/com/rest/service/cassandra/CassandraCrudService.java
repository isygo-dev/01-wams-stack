package eu.isygoit.com.rest.service.cassandra;

import eu.isygoit.com.rest.service.ICodifiableService;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.com.rest.service.impl.CrudServiceUtils;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyListException;
import eu.isygoit.exception.OperationNotAllowedException;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ISAASEntity;
import eu.isygoit.model.jakarta.CancelableEntity;
import eu.isygoit.repository.JpaPagingAndSortingSAASRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.NotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The type Cassandra crud service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CassandraCrudService<I, T extends IIdEntity, R extends CassandraRepository<T, I>> extends CrudServiceUtils<T, R>
        implements ICrudServiceMethod<I, T> {

    // Generic type of the entity class
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    @Transactional(readOnly = true)
    public Long count() {
        return repository().count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long count(String domain) {
        if (repository() instanceof JpaPagingAndSortingSAASRepository) {
            return ((JpaPagingAndSortingSAASRepository) repository()).countByDomainIgnoreCase(domain);
        } else {
            throw new UnsupportedOperationException("This is not a SAAS entity/repository: " + repository().getClass().getSimpleName());
        }
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
        validateEntity(object);
        object = beforeCreate(object);
        setCodeIfNeeded(object);
        return afterCreate(repository().save(object));
    }

    @Override
    @Transactional
    public T createAndFlush(T object) {
        return this.create(object);
    }

    @Override
    public List<T> create(List<T> objects) {
        return List.of();
    }

    @Override
    public void delete(String senderDomain, I id) {

    }

    @Override
    @Transactional
    public T update(T object) {
        validateEntity(object);
        validateEntityId(object);
        object = beforeUpdate(object);
        setCodeIfNeeded(object);
        return afterUpdate(repository().save(object));
    }

    @Override
    public T updateAndFlush(T object) {
        return null;
    }

    @Override
    public List<T> update(List<T> objects) {
        return List.of();
    }

    @Override
    public List<T> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria) {
        return List.of();
    }

    @Override
    public List<T> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria, PageRequest pageRequest) {
        return List.of();
    }

    @Override
    @Transactional
    public void delete(I id) {
        validateId(id);
        findById(id).ifPresent(object -> {
            beforeDelete(id);
            performDelete(object, id);
            afterDelete(id);
        });
    }

    @Override
    @Transactional
    public void delete(String senderDomain, List<T> objects) {
        validateEntityList(objects);
        checkSaaSDeletePermission(senderDomain, objects);
        beforeDelete(objects);
        repository().deleteAll(objects);
        afterDelete(objects);
    }

    @Override
    @Transactional
    public void delete(List<T> objects) {
        validateEntityList(objects);
        beforeDelete(objects);
        repository().deleteAll(objects);
        afterDelete(objects);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(I id) {
        validateId(id);
        Optional<T> optional = repository().findById(id);
        return optional.map(this::afterFindById);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return afterFindAll(repository().findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(Pageable pageable) {
        return afterFindAll(repository().findAll(pageable).getContent());
    }

    @Override
    public List<T> findAll(String domain, Pageable pageable) throws NotSupportedException {
        return List.of();
    }

    @Override
    public List<T> findAll(String domain) throws NotSupportedException {
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && repository() instanceof JpaPagingAndSortingSAASRepository) {
            return afterFindAll(((JpaPagingAndSortingSAASRepository) repository()).findByDomainIgnoreCase(domain));
        } else {
            throw new NotSupportedException("find all by domain for: " + persistentClass.getSimpleName());
        }
    }

    @Override
    @Transactional
    public T saveOrUpdate(T object) {
        validateEntity(object);
        return (object.getId() == null) ? create(object) : update(object);
    }

    @Override
    public List<T> saveOrUpdate(List<T> objects) {
        return List.of();
    }

    // Helper Methods
    private void validateEntity(T object) {
        if (object == null) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
    }

    private void validateEntityId(T object) {
        if (object.getId() == null) {
            throw new EntityNotFoundException();
        }
    }

    private void validateId(I id) {
        if (id == null) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
    }

    private void validateEntityList(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }
    }

    private void setCodeIfNeeded(T object) {
        if (object instanceof ICodifiable codifiable && !StringUtils.hasText(codifiable.getCode())) {
            if (this instanceof ICodifiableService codifiableService) {
                codifiableService.getNextCode().ifPresent(code -> codifiable.setCode((String) code));
            }
        }
    }

    private void checkSaaSDeletePermission(String senderDomain, List<T> objects) {
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            Optional<T> invalidDomainObject = objects.stream()
                    .filter(object -> !senderDomain.equals(((ISAASEntity) object).getDomain()))
                    .findFirst();
            invalidDomainObject.ifPresent(object -> {
                        throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " with id: " + object.getId());
                    }
            );
        }
    }

    private void performDelete(T object, I id) {
        if (CancelableEntity.class.isAssignableFrom(persistentClass)) {
            ((CancelableEntity) object).setCheckCancel(true);
            ((CancelableEntity) object).setCancelDate(new Date());
            update(object);
        } else {
            repository().deleteById(id);
        }
    }

    // Optional Overrides
    public T beforeUpdate(T object) {
        return object;
    }

    public T afterUpdate(T object) {
        return object;
    }

    public List<T> afterFindAll(List<T> list) {
        return list;
    }

    public T afterFindById(T object) {
        return object;
    }

    public void beforeDelete(List<T> objects) {
    }

    public void afterDelete(List<T> objects) {
    }

    public void beforeDelete(I id) {
    }

    public void afterDelete(I id) {
    }
}