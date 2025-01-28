package eu.isygoit.com.rest.service.cassandra;

import eu.isygoit.com.rest.service.ICodifiableService;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.com.rest.service.impl.CrudServiceUtils;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyListException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.OperationNotAllowedException;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ISAASEntity;
import eu.isygoit.model.jakarta.CancelableEntity;
import eu.isygoit.repository.JpaPagingAndSortingSAASRepository;
import jakarta.persistence.EntityExistsException;
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
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * The type Cassandra crud service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CassandraCrudService<I, T extends IIdEntity, R extends CassandraRepository> extends CrudServiceUtils<T, R>
        implements ICrudServiceMethod<I, T> {

    //Attention !!! should get the class type of th persist entity
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

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

        if (Objects.isNull(object.getId())) {
            object = this.beforeCreate(object);
            if (this instanceof ICodifiableService codifiableService &&
                    object instanceof ICodifiable codifiable &&
                    !StringUtils.hasText(codifiable.getCode())) {
                codifiableService.getNextCode().ifPresent(code -> codifiable.setCode((String) code));
            }
            return this.afterCreate((T) repository().save(object));
        } else {
            throw new EntityExistsException();
        }
    }

    @Override
    @Transactional
    public T createAndFlush(T object) {
        return this.create(object);
    }

    @Override
    public List<T> create(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        return objects.stream().map(object ->
                this.create(object)
        ).toList();
    }

    @Override
    @Transactional
    public T update(T object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (Objects.nonNull(object.getId())) {
            object = this.beforeUpdate(object);
            if (this instanceof ICodifiableService codifiableService &&
                    object instanceof ICodifiable codifiable &&
                    !StringUtils.hasText(codifiable.getCode())) {
                codifiableService.getNextCode().ifPresent(code -> codifiable.setCode((String) code));
            }
            return this.afterUpdate((T) repository().save(object));
        } else {
            throw new EntityNotFoundException();
        }
    }

    @Override
    @Transactional
    public T updateAndFlush(T object) {
        return this.update(object);
    }

    @Override
    @Transactional
    public List<T> update(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        return objects.stream().map(object ->
                this.update(object)
        ).toList();
    }

    @Override
    @Transactional
    public void delete(String senderDomain, List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        if (ISAASEntity.class.isAssignableFrom(persistentClass)
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            Optional<T> optional = objects.stream().filter(object -> !senderDomain.equals(((ISAASEntity) object).getDomain())).findFirst();
            if (optional.isPresent()) {
                throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " with id: " + optional.get().getId());
            }
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

        this.findById(id).ifPresent(object -> {
            //For SaaS Entity, check if delete ope is allowed : super domain or same domain
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
                if (!senderDomain.equals(((ISAASEntity) object).getDomain())) {
                    throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " with id: " + id);
                }
            }

            //before delete ope
            this.beforeDelete(id);

            if (CancelableEntity.class.isAssignableFrom(persistentClass)
                    || CancelableEntity.class.isAssignableFrom(persistentClass)) {
                ((CancelableEntity) object).setCheckCancel(true);
                ((CancelableEntity) object).setCancelDate(new Date());
                this.update(object);
            } else {
                repository().deleteById(id);
            }

            //after delete ope
            this.afterDelete(id);
        });
    }

    @Override
    @Transactional
    public void delete(List<T> objects) {
        if (ISAASEntity.class.isAssignableFrom(persistentClass)) {
            throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " should use SAAS delete");
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
        if (ISAASEntity.class.isAssignableFrom(persistentClass)) {
            throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " should use SAAS delete");
        }

        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        this.findById(id).ifPresent(object -> {
            //before delete ope
            this.beforeDelete(id);

            if (CancelableEntity.class.isAssignableFrom(persistentClass)
                    || CancelableEntity.class.isAssignableFrom(persistentClass)) {
                ((CancelableEntity) object).setCheckCancel(true);
                ((CancelableEntity) object).setCancelDate(new Date());
                this.update(object);
            } else {
                repository().deleteById(id);
            }

            //after delete ope
            this.afterDelete(id);
        });
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
        if (ISAASEntity.class.isAssignableFrom(persistentClass)
                && repository() instanceof JpaPagingAndSortingSAASRepository) {
            log.warn("Find all give vulnerability to SAS entity...");
        }

        List<T> list = repository().findAll();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return this.afterFindAll(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(Pageable pageable) {
        if (ISAASEntity.class.isAssignableFrom(persistentClass)
                && repository() instanceof JpaPagingAndSortingSAASRepository) {
            log.warn("Find all give vulnerability to SAS entity...");
        }

        Slice<T> page = repository().findAll(pageable);
        if (page.isEmpty()) {
            return Collections.emptyList();
        }

        return this.afterFindAll(page.getContent());
    }

    @Override
    public List<T> findAll(String domain) throws NotSupportedException {
        if (ISAASEntity.class.isAssignableFrom(persistentClass)
                && repository() instanceof JpaPagingAndSortingSAASRepository jpaPagingAndSortingSAASRepository) {
            List<T> list = jpaPagingAndSortingSAASRepository.findByDomainIgnoreCase(domain);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.emptyList();
            }
            return this.afterFindAll(list);
        } else {
            throw new NotSupportedException("find all by domain for :" + persistentClass.getSimpleName());
        }
    }

    @Override
    public List<T> findAll(String domain, Pageable pageable) throws NotSupportedException {
        if (ISAASEntity.class.isAssignableFrom(persistentClass)
                && repository() instanceof JpaPagingAndSortingSAASRepository jpaPagingAndSortingSAASRepository) {
            Page<T> page = jpaPagingAndSortingSAASRepository.findByDomainIgnoreCase(domain, pageable);
            if (page.isEmpty()) {
                return Collections.emptyList();
            }
            return this.afterFindAll(page.getContent());
        } else {
            throw new NotSupportedException("find all by domain for :" + persistentClass.getSimpleName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(I id) throws ObjectNotFoundException {
        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        Optional<T> optional = repository().findById(id);
        if (optional.isPresent()) {
            return Optional.ofNullable(this.afterFindById(optional.get()));
        }

        return Optional.empty();
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
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        return objects.stream().map(object ->
                this.saveOrUpdate(object)
        ).toList();
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
    public List<T> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria) {
        return Collections.emptyList();
    }

    @Override
    public List<T> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria, PageRequest pageRequest) {
        return Collections.emptyList();
    }
}
