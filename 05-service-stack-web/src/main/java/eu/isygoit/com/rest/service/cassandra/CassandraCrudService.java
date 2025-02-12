package eu.isygoit.com.rest.service.cassandra;

import eu.isygoit.com.rest.service.IAssignableCodeService;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.com.rest.service.impl.CrudServiceUtils;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyListException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.OperationNotAllowedException;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.model.IAssignableCode;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ISAASEntity;
import eu.isygoit.model.jakarta.CancelableEntity;
import eu.isygoit.repository.JpaPagingAndSortingSAASRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.NotSupportedException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * The type Cassandra crud service.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CassandraCrudService<I extends Serializable, E extends IIdEntity, R extends CassandraRepository> extends CrudServiceUtils<I, E, R>
        implements ICrudServiceMethod<I, E> {

    //Attention !!! should get the class type of th persist entity
    @Getter
    private final Class<E> persistentClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    @Transactional(readOnly = true)
    public Long count() {
        return repository().count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long count(String domain) {
        if (repository() instanceof JpaPagingAndSortingSAASRepository jpaPagingAndSortingSAASRepository) {
            return jpaPagingAndSortingSAASRepository.countByDomainIgnoreCase(domain);
        } else {
            throw new UnsupportedOperationException("this is not a SAS entity/repository: " + repository().getClass().getSimpleName());
        }
    }

    @Override
    public boolean existsById(I id) {
        return repository().existsById(id);
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
    @Transactional
    public E create(E object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (object.getId() == null) {
            object = this.beforeCreate(object);
            if (this instanceof IAssignableCodeService assignableCodeService &&
                    object instanceof IAssignableCode codifiable &&
                    !StringUtils.hasText(codifiable.getCode())) {
                codifiable.setCode(assignableCodeService.getNextCode());
            }
            return this.afterCreate((E) repository().save(object));
        } else {
            throw new EntityExistsException();
        }
    }

    @Override
    @Transactional
    public E createAndFlush(E object) {
        return this.create(object);
    }

    @Override
    public List<E> create(List<E> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        List<E> createdObjects = new ArrayList<>();
        objects.forEach(object -> {
            createdObjects.add(this.create(object));
        });

        return createdObjects;
    }

    @Override
    @Transactional
    public E update(E object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (object.getId() != null) {
            object = this.beforeUpdate(object);
            if (this instanceof IAssignableCodeService assignableCodeService &&
                    object instanceof IAssignableCode codifiable &&
                    !StringUtils.hasText(codifiable.getCode())) {
                codifiable.setCode(assignableCodeService.getNextCode());
            }
            return this.afterUpdate((E) repository().save(object));
        } else {
            throw new EntityNotFoundException();
        }
    }

    @Override
    @Transactional
    public E updateAndFlush(E object) {
        return this.update(object);
    }

    @Override
    @Transactional
    public List<E> update(List<E> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }
        List<E> updatedObjects = new ArrayList<>();
        objects.forEach(object -> {
            updatedObjects.add(this.update(object));
        });

        return updatedObjects;
    }

    @Override
    @Transactional
    public void delete(String senderDomain, List<E> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            objects.forEach(object -> {
                if (!senderDomain.equals(((ISAASEntity) object).getDomain())) {
                    throw new OperationNotAllowedException("Delete " + getPersistentClass().getSimpleName() + " with id: " + object.getId());
                }
            });
        }

        this.beforeDelete(objects);
        repository().deleteAll(objects);
        this.afterDelete(objects);
    }

    @Override
    @Transactional
    public void delete(String senderDomain, I id) {
        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        E object = this.findById(id);
        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            if (!senderDomain.equals(((ISAASEntity) object).getDomain())) {
                throw new OperationNotAllowedException("Delete " + getPersistentClass().getSimpleName() + " with id: " + id);
            }
        }

        this.beforeDelete(id);
        if (CancelableEntity.class.isAssignableFrom(getPersistentClass())
                || CancelableEntity.class.isAssignableFrom(getPersistentClass())) {
            ((CancelableEntity) object).setCheckCancel(true);
            ((CancelableEntity) object).setCancelDate(new Date());
            this.update(object);
        } else {
            repository().deleteById(id);
        }
        this.afterDelete(id);
    }

    @Override
    @Transactional
    public void delete(List<E> objects) {
        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())) {
            throw new OperationNotAllowedException("Delete " + getPersistentClass().getSimpleName() + " should use SAAS delete");
        }

        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }
        this.beforeDelete(objects);
        repository().deleteAll(objects);
        this.afterDelete(objects);
    }

    @Override
    @Transactional
    public void delete(I id) {
        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())) {
            throw new OperationNotAllowedException("Delete " + getPersistentClass().getSimpleName() + " should use SAAS delete");
        }

        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        E object = this.findById(id);
        this.beforeDelete(id);
        if (CancelableEntity.class.isAssignableFrom(getPersistentClass())
                || CancelableEntity.class.isAssignableFrom(getPersistentClass())) {
            ((CancelableEntity) object).setCheckCancel(true);
            ((CancelableEntity) object).setCancelDate(new Date());
            this.update(object);
        } else {
            repository().deleteById(id);
        }
        this.afterDelete(id);
    }

    @Override
    public void beforeDelete(I id) {
    }

    @Override
    public void afterDelete(I id) {
    }

    @Override
    public void beforeDelete(List<E> objects) {
    }

    @Override
    public void afterDelete(List<E> objects) {
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> findAll() {
        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())
                && repository() instanceof JpaPagingAndSortingSAASRepository) {
            log.warn("Find all give vulnerability to SAS entity...");
        }

        List<E> list = repository().findAll();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }

        return this.afterFindAll(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> findAll(Pageable pageable) {
        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())
                && repository() instanceof JpaPagingAndSortingSAASRepository) {
            log.warn("Find all give vulnerability to SAS entity...");
        }

        Slice<E> page = repository().findAll(pageable);
        if (page.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        return this.afterFindAll(page.getContent());
    }

    @Override
    public List<E> findAll(String domain) throws NotSupportedException {
        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())
                && repository() instanceof JpaPagingAndSortingSAASRepository jpaPagingAndSortingSAASRepository) {
            List<E> list = jpaPagingAndSortingSAASRepository.findByDomainIgnoreCase(domain);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.EMPTY_LIST;
            }
            return this.afterFindAll(list);
        } else {
            throw new NotSupportedException("find all by domain for :" + getPersistentClass().getSimpleName());
        }
    }

    @Override
    public List<E> findAll(String domain, Pageable pageable) throws NotSupportedException {
        if (ISAASEntity.class.isAssignableFrom(getPersistentClass())
                && repository() instanceof JpaPagingAndSortingSAASRepository jpaPagingAndSortingSAASRepository) {
            Page<E> page = jpaPagingAndSortingSAASRepository.findByDomainIgnoreCase(domain, pageable);
            if (page.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            return this.afterFindAll(page.getContent());
        } else {
            throw new NotSupportedException("find all by domain for :" + getPersistentClass().getSimpleName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public E findById(I id) throws ObjectNotFoundException {
        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
        Optional<E> optional = repository().findById(id);
        if (optional.isPresent()) {
            return this.afterFindById(optional.get());
        }
        return null;
    }

    @Override
    @Transactional
    public E saveOrUpdate(E object) {
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
}
